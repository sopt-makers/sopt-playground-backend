package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.domain.Sopticle;
import org.sopt.makers.internal.domain.SopticleWriter;
import org.sopt.makers.internal.dto.sopticle.SopticleDao;
import org.sopt.makers.internal.dto.sopticle.SopticleSaveRequest;
import org.sopt.makers.internal.dto.sopticle.SopticleVo;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.DuplicateSopticleWriterException;
import org.sopt.makers.internal.external.OfficialHomeClient;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.SopticleQueryRepository;
import org.sopt.makers.internal.repository.SopticleRepository;
import org.sopt.makers.internal.repository.SopticleWriterRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SopticleService {
    private final AuthConfig authConfig;
    private final SopticleRepository sopticleRepository;
    private final SopticleWriterRepository sopticleWriterRepository;
    private final MemberRepository memberRepository;
    private final SopticleQueryRepository sopticleQueryRepository;
    private final OfficialHomeClient officialHomeClient;

    @Transactional
    public Sopticle createSopticle (SopticleSaveRequest request, Long userId) {
        val writers = Set.copyOf(request.writerIds());
        val isValidNumberOfWriters = memberRepository.countByIdIn(writers) == writers.size();
        if (!isValidNumberOfWriters) throw new DuplicateSopticleWriterException("DupWriters");
        val sopticle = sopticleRepository.save(Sopticle.builder()
                .link(request.link())
                .userId(userId)
                .registeredAt(LocalDateTime.now())
                .build());
        val sopticleWriters = writers.stream().map(id ->
                SopticleWriter.builder()
                        .writerId(id)
                        .sopticleId(sopticle.getId())
                        .build()).toList();
        sopticleWriterRepository.saveAll(sopticleWriters);
        val joinedSopticleList = sopticleQueryRepository.findById(sopticle.getId());
        val sopticleOfficialRequestBody = getSopticleVo(joinedSopticleList);
        officialHomeClient.createSopticle(authConfig.getOfficialSopticleApiSecretKey(), sopticleOfficialRequestBody.get(0));
        return sopticle;
    }

    private List<SopticleVo> getSopticleVo(List<SopticleDao> joinedSopticleList) {
        val sopticleWriterMap = joinedSopticleList.stream().collect(
                Collectors.groupingBy(SopticleDao::id, HashMap::new,
                        Collectors.mapping(SopticleDao::memberId, Collectors.toSet()))
        );
        val linkMap = joinedSopticleList.stream()
                .collect(Collectors.toMap(SopticleDao::id, SopticleDao::link, (p1, p2) -> p1));
        val writerMap = joinedSopticleList.stream().map(s -> new SopticleVo.SopticleUserVo(
                s.memberId(), s.name(), s.profileImage(), s.part(), s.generation()
        )).collect(Collectors.groupingBy(SopticleVo.SopticleUserVo::id));

        return sopticleWriterMap.keySet().stream().map(sopticleId -> {
            val link = linkMap.get(sopticleId);
            val writerIdSet = sopticleWriterMap.get(sopticleId);
            val writers = writerIdSet.stream().map(id -> writerMap.get(id).stream().max(
                    Comparator.comparing(SopticleVo.SopticleUserVo::generation)).get()
            ).toList();
            return new SopticleVo(sopticleId, link, writers);
        }).toList();
    }
}

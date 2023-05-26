package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.Sopticle;
import org.sopt.makers.internal.domain.SopticleWriter;
import org.sopt.makers.internal.dto.SopticleSaveRequest;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.SopticleRepository;
import org.sopt.makers.internal.repository.SopticleWriterRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class SopticleService {
    private final SopticleRepository sopticleRepository;
    private final SopticleWriterRepository sopticleWriterRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Sopticle createSopticle (SopticleSaveRequest request, Long userId) {
        val writers = Set.copyOf(request.writerIds());
        val isValidNumberOfWriters = memberRepository.countByIdIn(writers) == writers.size();
        if (!isValidNumberOfWriters) throw new ClientBadRequestException("Wrong writers");
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
        return sopticle;
    }
}

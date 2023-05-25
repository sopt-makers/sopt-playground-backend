package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.Sopticle;
import org.sopt.makers.internal.domain.SopticleWriter;
import org.sopt.makers.internal.dto.SopticleSaveRequest;
import org.sopt.makers.internal.repository.SopticleRepository;
import org.sopt.makers.internal.repository.SopticleWriterRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class SopticleService {
    private final SopticleRepository sopticleRepository;
    private final SopticleWriterRepository sopticleWriterRepository;

    @Transactional
    public Sopticle createSopticle (SopticleSaveRequest request, Long userId) {
        val sopticle = sopticleRepository.save(Sopticle.builder()
                .link(request.link())
                .userId(userId)
                .registeredAt(LocalDateTime.now())
                .build());
        val sopticleWriters = request.writerIds().stream().map(id ->
                SopticleWriter.builder()
                        .writerId(id)
                        .sopticleId(sopticle.getId())
                        .build()).toList();
        sopticleWriterRepository.saveAll(sopticleWriters);
        return sopticle;
    }
}

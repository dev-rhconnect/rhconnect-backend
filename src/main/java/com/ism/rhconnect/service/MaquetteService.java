package com.ism.rhconnect.service;

import com.ism.rhconnect.dto.request.MaquetteModuleRequest;
import com.ism.rhconnect.dto.response.MaquetteModuleResponse;
import com.ism.rhconnect.entity.ClasseRef;
import com.ism.rhconnect.entity.MaquetteModule;
import com.ism.rhconnect.entity.ModuleRef;
import com.ism.rhconnect.exception.ResourceNotFoundException;
import com.ism.rhconnect.repository.ClasseRefRepository;
import com.ism.rhconnect.repository.MaquetteModuleRepository;
import com.ism.rhconnect.repository.ModuleRefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaquetteService {

    private final MaquetteModuleRepository maquetteRepository;
    private final ClasseRefRepository classeRefRepository;
    private final ModuleRefRepository moduleRefRepository;

    @Transactional(readOnly = true)
    public List<MaquetteModuleResponse> listerTous() {
        return maquetteRepository.findByActifTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaquetteModuleResponse> listerParClasse(String classeNom) {
        return maquetteRepository.findByClasseRefNomAndActifTrue(classeNom).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retourne le volume horaire prévu pour un module dans une classe donnée.
     * Retourne null si la combinaison n'est pas dans la maquette.
     */
    @Transactional(readOnly = true)
    public Optional<Double> getVolumeHoraire(String classeNom, String moduleNom) {
        return maquetteRepository.findByClasseRefNomAndModuleRefNom(classeNom, moduleNom)
                .map(MaquetteModule::getVolumeHoraire);
    }

    /**
     * Calcule le volume horaire total d'un module sur plusieurs classes.
     * Utilisé lors de la création du contrat.
     */
    @Transactional(readOnly = true)
    public double getVolumeHoraireTotal(String moduleNom, List<String> classes) {
        return classes.stream()
                .mapToDouble(c -> maquetteRepository
                        .findByClasseRefNomAndModuleRefNom(c, moduleNom)
                        .map(MaquetteModule::getVolumeHoraire)
                        .orElse(0.0))
                .sum();
    }

    @Transactional
    public MaquetteModuleResponse creerOuModifier(MaquetteModuleRequest req) {
        ClasseRef classe = classeRefRepository.findByNom(req.getClasseNom())
                .orElseThrow(() -> new ResourceNotFoundException("Classe introuvable : " + req.getClasseNom()));
        ModuleRef module = moduleRefRepository.findByNom(req.getModuleNom())
                .orElseThrow(() -> new ResourceNotFoundException("Module introuvable : " + req.getModuleNom()));

        MaquetteModule maquette = maquetteRepository
                .findByClasseRefNomAndModuleRefNom(req.getClasseNom(), req.getModuleNom())
                .orElseGet(() -> MaquetteModule.builder().classeRef(classe).moduleRef(module).build());

        maquette.setVolumeHoraire(req.getVolumeHoraire());
        maquette.setActif(true);
        return toResponse(maquetteRepository.save(maquette));
    }

    @Transactional
    public void supprimer(Long id) {
        MaquetteModule m = maquetteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrée maquette introuvable : " + id));
        m.setActif(false);
        maquetteRepository.save(m);
    }

    private MaquetteModuleResponse toResponse(MaquetteModule m) {
        return MaquetteModuleResponse.builder()
                .id(m.getId())
                .classeNom(m.getClasseRef().getNom())
                .classeFiliere(m.getClasseRef().getFiliere())
                .moduleNom(m.getModuleRef().getNom())
                .volumeHoraire(m.getVolumeHoraire())
                .actif(m.isActif())
                .build();
    }
}

package com.bbm.beprepared.services.impl;

import com.bbm.beprepared.exception.BadRequestException;
import com.bbm.beprepared.exception.EntityNotFoundException;
import com.bbm.beprepared.model.Citizen;
import com.bbm.beprepared.model.City;
import com.bbm.beprepared.model.enums.Role;
import com.bbm.beprepared.repository.CitizenRepository;
import com.bbm.beprepared.services.CitizenService;
import com.bbm.beprepared.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CitizenServiceImpl implements CitizenService {

    private final PasswordEncoder passwordEncoder;
    private final LocationService locationService;
    private final CitizenRepository citizenRepository;


    @Override
    @Transactional
    public String createCitizen(Citizen citizen, Long cityId) {
        if(citizenRepository.existsByPhone(citizen.getPhone())) {
            throw new BadRequestException("Este número já está sendo utilizado!");
        }
        City city = locationService.getCityById(cityId);
        citizen.setCity(city);
        citizen.setVerified(false);
        citizen.setRole(Role.USER);
        citizen.setOtp(generateOtp(6));
        var savedCitizen = citizenRepository.save(citizen);
        return "Cidadão criado com sucesso! O seu código de verificação é: " + savedCitizen.getOtp();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Citizen> getAllCitizens() {
        return citizenRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Citizen> getAllCitizenByCityId(Long cityId) {
        return citizenRepository.findAllByCityId(cityId);
    }

    @Override
    public List<Citizen> getAllCitizenByProvinceId(Long provinceId) {
        return citizenRepository.findAllByCityProvinceId(provinceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Citizen getCitizenById(Long id) {
        return citizenRepository.findById(id).orElseThrow(()->
                new EntityNotFoundException("Cidadão não encontradoo!!"));
    }

    @Override
    @Transactional
    public String verifyAccount(String otp) {
        Citizen citizen = citizenRepository.findByOtp(otp).orElseThrow(()->
                new EntityNotFoundException("Cidadão não encontrado!!!"));
        citizen.setVerified(true);
        citizen.setOtp(null);
        citizenRepository.save(citizen);
        return "A tua conta foi verificada com sucesso!";
    }

    @Override
    @Transactional
    public String generateOTPForCitizen(String phone) {
        Citizen citizen = citizenRepository.findByPhone(phone).orElseThrow(()->
                new EntityNotFoundException("Cidadão não encontrado, não foi possível gerar seu código de verificação!"));

        citizen.setOtp(null);
        String otp = generateOtp(6);
        citizen.setOtp(passwordEncoder.encode(otp));
        return "O seu código de acesso é: " + otp;
    }

    public static String generateOtp(int length){
        String otp= "";
        int x;
        char[] chars = new char[length];

        for (int i = 0; i < length; i++){
            Random random = new Random();
            x = random.nextInt(9);
            chars[i] = Integer.toString(x).toCharArray()[0];
        }

        otp = new String(chars);
        return otp.trim();
    }
}

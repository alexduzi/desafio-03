package com.alexduzi.desafio03.services;

import com.alexduzi.desafio03.dto.ClientDTO;
import com.alexduzi.desafio03.entities.Client;
import com.alexduzi.desafio03.exceptions.ClientNotFoundException;
import com.alexduzi.desafio03.exceptions.DatabaseException;
import com.alexduzi.desafio03.exceptions.PropertyEntityException;
import com.alexduzi.desafio03.repositories.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public ClientDTO findById(Long id) {
        return repository.findById(id).map(this::convertToDto).orElseThrow(() -> new ClientNotFoundException("Cliente não foi encontrado!"));
    }

    @Transactional(readOnly = true)
    public Page<ClientDTO> findAll(Pageable pageable) {
        try {
            return repository.findAll(pageable).map(this::convertToDto);
        } catch (PropertyReferenceException e) {
            throw new PropertyEntityException("Propriedade utilizada na busca não existe no {Cliente}");
        }
    }

    @Transactional
    public ClientDTO insert(ClientDTO dto) {
        Client client = convertToEntity(dto);
        client = repository.save(client);
        return convertToDto(client);
    }

    @Transactional
    public ClientDTO update(Long id, ClientDTO dto) {
        try {
            Client client = repository.getReferenceById(id);
            copyDtoToEntity(dto, client);
            client = repository.save(client);
            return convertToDto(client);
        } catch (EntityNotFoundException e) {
            throw new ClientNotFoundException("Cliente não foi encontrado!");
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ClientNotFoundException("Cliente não foi encontrado!");
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial");
        }
    }


    private ClientDTO convertToDto(Client client) {
        return modelMapper.map(client, ClientDTO.class);
    }

    private Client convertToEntity(ClientDTO clientDTO) {
        return modelMapper.map(clientDTO, Client.class);
    }

    private void copyDtoToEntity(ClientDTO dto, Client entity) {
        entity.setCpf(dto.getCpf());
        entity.setName(dto.getName());
        entity.setIncome(dto.getIncome());
        entity.setChildren(dto.getChildren());
        entity.setBirthDate(dto.getBirthDate());
    }
}

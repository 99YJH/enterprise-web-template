package com.enterprise.webtemplate.repository;

import com.enterprise.webtemplate.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r WHERE r.isSystemRole = true")
    List<Role> findSystemRoles();

    @Query("SELECT r FROM Role r WHERE r.isSystemRole = false")
    List<Role> findCustomRoles();

    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    List<Role> findByNames(@Param("names") List<String> names);
}
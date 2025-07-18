package com.enterprise.webtemplate.repository;

import com.enterprise.webtemplate.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource")
    List<Permission> findByResource(@Param("resource") String resource);

    @Query("SELECT p FROM Permission p WHERE p.action = :action")
    List<Permission> findByAction(@Param("action") String action);

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action")
    Optional<Permission> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

    @Query("SELECT p FROM Permission p WHERE p.isSystemPermission = true")
    List<Permission> findSystemPermissions();

    @Query("SELECT p FROM Permission p WHERE p.isSystemPermission = false")
    List<Permission> findCustomPermissions();

    @Query("SELECT p FROM Permission p WHERE p.name IN :names")
    List<Permission> findByNames(@Param("names") List<String> names);
}
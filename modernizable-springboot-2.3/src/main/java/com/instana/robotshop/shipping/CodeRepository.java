package com.instana.robotshop.shipping;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeRepository extends JpaRepository<Code, Long> {

    Code findById(long id);
}

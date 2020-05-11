package org.sid.dao;


import java.sql.Date;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import javax.persistence.Table;

import org.sid.entities.Client;
import org.sid.entities.Formation;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import org.springframework.transaction.annotation.Transactional;

@RepositoryRestResource
public interface FormationRepository extends JpaRepository<Formation, Long> {
	
	@Query(value="select f.*,avg(r.count_stars) from formation f, rating r where traing=id group by traing",nativeQuery =true)
	public  Page<Formation> findAllAndRating(Pageable paging);
	
	@Query("select f from Formation f where user_id like :x")
	public List<Formation> findByUserId(@Param("x") Long id);
	
	@Query("select f from Formation f where Article_Cat like :x")
	public Page<Formation> findByArticleCat(@Param("x") String cat,Pageable paging);
	
	@Query("select count(*) from Formation f where Article_Cat like :x")
	public Long countByArticleCat(@Param("x") String cat);
	
	@Transactional
	@Modifying
	@Query(value="insert into Formationreservee (user_id,training_id) values (:userId,:trainingId)",nativeQuery = true)
	public void insertIntoReservation(@Param("userId") Long uId,@Param("trainingId") Long tId);
	
	@Query(value="select count(*) from Formationreservee f where training_id like :x",nativeQuery = true)
	public Long countByIdFormation(@Param("x") Long id);
	
	@Query(value="select training_id from Formationreservee f where training_id like :x and  user_id like :y",nativeQuery = true)
	public List<Long> verifyIfAlreadyExist(@Param("y") Long uId,@Param("x") Long tId);
	
	@Query(value="select * from Formation fo  where  id in (select training_id from formationreservee f where user_id like :x)",nativeQuery = true)
	public List<Formation> findReservedTraining(@Param("x") Long id);
	
	@Transactional
	@Modifying
	@Query(value="delete from formationreservee where training_id like :x and user_id like :y",nativeQuery = true)
	public void deleteMyReservation(@Param("x") Long tId,@Param("y") Long uId);
	
	@Query(value="select * from formation f where f.user_id in (select c.id from client c where nom like :x ) and f.local in (select l.id from local l where ville like:y) and ( :z between f.first_day and last_day)",nativeQuery=true)
	public Page<Formation> rechercherformation(@Param("x") String TrainerName, @Param("y") String Local,@Param("z") Date date,Pageable paging);
	
	@Query(value="select * from formation where first_day like DATE_SUB(CURDATE(), INTERVAL 1 DAY) and CURDATE()", nativeQuery = true)
	public List<Formation> trendyTrainings();
	
	
}

package ru.javawebinar.topjava.repository.datajpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Meal;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
public interface CrudMealRepository extends JpaRepository<Meal, Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM Meal m WHERE m.id=:id AND m.user.id=:userid")
    int delete(@Param("id") int id, @Param("userid") int userId);

    @Transactional
    @Query("SELECT m FROM Meal m WHERE m.id=:id AND m.user.id=:userid")
    Meal getUserMeal(@Param("id") int id, @Param("userid") int userId);

    @Transactional
    @Query("SELECT m FROM Meal m WHERE m.user.id=:userid ORDER BY m.dateTime DESC")
    List<Meal> getUserAllMeals(@Param("userid") int userId);

    @Transactional
    @Query("SELECT m FROM Meal m WHERE m.user.id=:userid AND m.dateTime>=:startDate AND m.dateTime<:endDate ORDER BY m.dateTime DESC")
    List<Meal> getUserAllMealsDateFiltered(@Param("userid") int userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
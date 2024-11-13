package com.cos.jwt.mapper;

import com.cos.jwt.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {

    public int insertUser(User user);

    public User selectByUsername(String username);
}

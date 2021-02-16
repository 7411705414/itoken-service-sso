package com.funtl.itoken.service.sso.mapper;



import com.funtl.itoken.common.domain.TbSysUser;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.MyMapper;


@Component
public interface TbSysUserMapper extends MyMapper<TbSysUser> {
}
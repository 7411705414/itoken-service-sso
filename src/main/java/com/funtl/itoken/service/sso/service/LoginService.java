package com.funtl.itoken.service.sso.service;

import com.funtl.itoken.common.domain.TbSysUser;

public interface LoginService {

    public TbSysUser login(String loginCode, String plantPassword) ;

}

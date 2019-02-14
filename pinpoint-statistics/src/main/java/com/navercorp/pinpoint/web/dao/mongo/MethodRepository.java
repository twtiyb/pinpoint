package com.navercorp.pinpoint.web.dao.mongo;

import com.navercorp.pinpoint.web.vo.link.Method;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by chunchun.xu on 2018/11/22.
 */

@Repository
public interface MethodRepository extends MongoRepository<Method, String> {

}

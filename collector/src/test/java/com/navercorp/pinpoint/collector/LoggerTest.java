/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector;

import org.apache.hadoop.hbase.util.MD5Hash;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author emeroad
 */
public class LoggerTest {
    @Test
    public void log() {
        Logger test = LoggerFactory.getLogger(LoggerTest.class);
        test.info("info");
        test.debug("debug");
    }


    @Test
    public void name() {
        String cryptStr = MD5Hash.getMD5AsHex(("abvbbbbbbbsbbbbbbbbbbbbbbbbbbbbbbb" + new Date().getTime()).getBytes());
        String cryptStr2 = MD5Hash.getMD5AsHex(("abvbbbbbbbsbbbbbbbbbbbbbbbbbbbbbbb2" + new Date().getTime()).getBytes());
        System.out.println(cryptStr);
        System.out.println(cryptStr2);
    }
}

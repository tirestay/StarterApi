package com.starter.track;

import com.common.http.ParamUtil;
import com.common.http.RespUtil;
import com.common.util.LogUtil;
import com.common.util.StrUtil;
import com.starter.annotation.AccessLimited;
import com.starter.mq.MqService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Topic;
import java.util.Map;

@Api(tags = {"代驾定位系统"})
@RestController
@RequestMapping("/track")
public class TrackController {
    @Autowired
    MqService mqService;

    @Autowired
    Topic trackPosition;

    @AccessLimited(count = 100)
    @ApiOperation("位置信息")
    @PostMapping("/{uid}")
    public Object track(@PathVariable String uid, @RequestBody String body) {
        LogUtil.info("/track", uid, body);

        ParamUtil paramUtil = new ParamUtil(body);
        Map<String, Object> paramMap = paramUtil.getMap();
        paramMap.put("uid", uid);
        paramMap.put("time", System.currentTimeMillis());
        mqService.sendMsg(trackPosition, paramMap);

        return RespUtil.ok(String.format("%s: %s",
                StrUtil.isEmpty(uid) ? "" : uid.substring(uid.length() - 2),
                paramUtil.getStr("addr"))
        );
    }

    @AccessLimited(count = 10)
    @ApiOperation("查询信息")
    @GetMapping
    public Object info() {
        LogUtil.info("/track/info");

        Map<String, Object> ret = RespUtil.ok();
        ret.put("items", TrackConsumer.clientMap.values());
        return ret;
    }
}

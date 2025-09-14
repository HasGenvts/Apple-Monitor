package top.misec.applemonitor.job;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import top.misec.applemonitor.config.AliveAlterConfig;
import top.misec.applemonitor.config.AppCfg;
import top.misec.applemonitor.config.CfgSingleton;
import top.misec.applemonitor.config.PushConfig;
import top.misec.applemonitor.push.impl.FeiShuBotPush;
import top.misec.applemonitor.push.pojo.feishu.FeiShuPushDTO;
import top.misec.bark.BarkPush;
import top.misec.bark.enums.SoundEnum;
import top.misec.bark.pojo.PushDetails;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Slf4j
public class AppAliveAlterMonitor {
    private final AppCfg CONFIG = CfgSingleton.getInstance().config;

    public void alter() {
        AliveAlterConfig aliveAlterConfig = CONFIG.getAppleTaskConfig().getAliveAlterConfig();

        PushConfig push = aliveAlterConfig.getPushConfig();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (!Objects.isNull(aliveAlterConfig.getIgnoreEndHour()) && !Objects.isNull(aliveAlterConfig.getIgnoreEndHour())) {

            // 获取当前小时
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); // 24小时制

            // 解析免打扰开始和结束小时（假设存储的是纯数字，如8、18等）
            int startHour = aliveAlterConfig.getIgnoreStartHour();
            int endHour = aliveAlterConfig.getIgnoreEndHour();

            // 判断当前时间是否在免打扰时间段内
            boolean isInIgnorePeriod;
            if (startHour <= endHour) {
                // 时间段不跨天（如 8 - 18）
                isInIgnorePeriod = currentHour >= startHour && currentHour < endHour;
            } else {
                // 时间段跨天（如 22 - 6）
                isInIgnorePeriod = currentHour >= startHour || currentHour < endHour;
            }
            // 不发送消息推送

            if (isInIgnorePeriod){
                log.info("在免打扰时间段 " + aliveAlterConfig.getIgnoreStartHour() + " ~ "  + aliveAlterConfig.getIgnoreEndHour()  + " , 不推送");
                return;
            }
        }


        String content = "当前机器人正常运行中 " + dateFormat.format(new Date());

        if (StrUtil.isAllNotEmpty(push.getBarkPushUrl(), push.getBarkPushToken())) {
            BarkPush barkPush = new BarkPush(push.getBarkPushUrl(), push.getBarkPushToken());
            PushDetails pushDetails = PushDetails.builder()
                    .title("机器人存活 提醒")
                    .body(content)
                    .category("机器人存活 提醒")
                    .group("Apple Monitor")
                    .sound(StrUtil.isEmpty(push.getBarkPushSound()) ? SoundEnum.GLASS.getSoundName() : push.getBarkPushSound())
                    .build();
            barkPush.simpleWithResp(pushDetails);
        }
        if (StrUtil.isAllNotEmpty(push.getFeishuBotSecret(), push.getFeishuBotWebhooks())) {

            FeiShuBotPush.pushTextMessage(FeiShuPushDTO.builder()
                    .text(content).secret(push.getFeishuBotSecret())
                    .botWebHooks(push.getFeishuBotWebhooks())
                    .build());
        }
    }
}

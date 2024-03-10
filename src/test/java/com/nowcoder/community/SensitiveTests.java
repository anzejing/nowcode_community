package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@PropertySource(value = "classpath:application.properties")
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void  testSensitiveFilter(){
        String text = "这里可以☆嫖☆娼☆，可以☆吸☆毒☆，可以赌☆博，可以☆开票☆,好▤▤好好";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "这里可以嫖娼，可以吸毒，可以赌博，可以开票,好▤▤好好";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

    }
}

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.ManageOrderApplication;
import com.xuecheng.order.dao.XcTaskRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(classes = ManageOrderApplication.class)
@RunWith(SpringRunner.class)
public class TestLocalDateTime {


    @Autowired
    XcTaskRepository xcTaskRepository;
    @Test
    public void test1(){
        Pageable pageable = PageRequest.of(0,10);
        Page<XcTask> all = xcTaskRepository.findByUpdateTimeBefore(pageable,LocalDateTime.now().minusDays(1));
        List<XcTask> content = all.getContent();
        System.out.println(content);
    }
}

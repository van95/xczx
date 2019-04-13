import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media.ManageMediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ManageMediaApplication.class)
@RunWith(SpringRunner.class)
public class Mp4Test {


    @Test
    public void test(){
        String ffmpeg_path= "D:/ffmpeg-20180227-fa0c9d6-win64-static/bin/ffmpeg.exe";
        String video_path = "D:/MediaVideo/9/4/94ff5398e4916c09de16127739369a86/lucene.avi";
        String mp4_name = "lucene.mp4";
        String mp4folder_path = "D:/MediaVideo/9/4/94ff5398e4916c09de16127739369a86/";
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
        String s = mp4VideoUtil.generateMp4();
        System.out.println(s);

    }
}

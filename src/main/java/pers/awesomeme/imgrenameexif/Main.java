package pers.awesomeme.imgrenameexif;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

public class Main
{
    public static void main(String[] args)
    {
        Optional<Date> picDate = getPicDate("/Users/awesome/my/download/01.实体收藏位置.HEIC");
        if (picDate.isEmpty())
        {
            Console.log("输出数据【{}】", "获取失败");
            return;
        }

        Console.log("输出数据【{}】", DateUtil.formatDateTime(picDate.get()));
    }

    /**
     * 读取图片/照片的拍摄时间
     * @param filePath 文件路径，必须是图片.
     * @return 时间
     */
    public static Optional<Date> getPicDate(String filePath)
    {
        // 读取元数据
        Metadata metadata;
        try
        {
            metadata = ImageMetadataReader.readMetadata(FileUtil.file(filePath));
        }
        catch (Exception e)
        {
            return Optional.empty();
        }

        // 从元数据中读取日期与时间
        ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (Objects.isNull(exifIFD0Directory))
        {
            return Optional.empty();
        }
        Date date = exifIFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME, TimeZone.getTimeZone("Asia/Shanghai"));
        return Objects.nonNull(date) ? Optional.of(date) : Optional.empty();
    }
}

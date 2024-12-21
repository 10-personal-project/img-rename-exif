package pers.awesomeme.imgrenameexif;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import pers.awesomeme.commoncode.Constants;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

public class Main
{
    public static void main(String[] args)
    {
        // 检查文件夹
        String dir = "/Users/awesome/my/download/姥爷";
        if (StrUtil.contains(dir, "CloudStorage/Dropbox"))
        {
            Console.log("不能是Dropbox里的文件夹，复制一个操作.");
            return;
        }
        if (!FileUtil.isDirectory(dir))
        {
            Console.log("【{}】不是文件夹", dir);
            return;
        }
        
        // 读取文件夹中所有的 HEIC 与 JPG 文件
        List<File> fileList = FileUtil.loopFiles(dir, pathname -> isHeicOrJpg(pathname.getAbsolutePath()));
        
        // 给文件进行重命名
        fileList.forEach(el -> rename(el.getAbsolutePath()));
    }

    /**
     * 读取图片/照片的拍摄时间 <br/>
     * 已测试过格式：HEIC、JPG
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
        if (Objects.isNull(date))
        {
            date = exifIFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME_ORIGINAL, TimeZone.getTimeZone("Asia/Shanghai"));
        }
        if (Objects.isNull(date))
        {
            date = exifIFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME_DIGITIZED, TimeZone.getTimeZone("Asia/Shanghai"));
        }
        return Objects.nonNull(date) ? Optional.of(date) : Optional.empty();
    }

    /**
     * 重命名一个图片
     * @param filePath 文件路径
     */
    public static void rename(String filePath)
    {
        // 获取新文件名，不包括"."与扩展名.
        Console.log("正在重命名中...");
        Optional<Date> picDate = getPicDate(filePath);
        if (picDate.isEmpty())
        {
            Console.log("【{}】重命名失败，获取日期与时间失败.", filePath);
            return;
        }
        String newName = DateUtil.format(picDate.get(), "yyyyMMdd_HHmm");

        // 进行重命名
        try
        {
            FileUtil.rename(FileUtil.file(filePath), newName, true, true);
        }
        catch (Exception e)
        {
            Console.log("【{}】重命名失败，要重命名的已存在.", filePath);
        }
    }

    /**
     * 根据文件名判断是不是HEIC或JPG
     * @param filePath 文件路径
     * @return true-是 false-不是
     */
    private static boolean isHeicOrJpg(String filePath)
    {
        if (!FileUtil.isFile(filePath))
        {
            return false;
        }

        int lastIndexOfDot = filePath.lastIndexOf(StrUtil.DOT);
        if (lastIndexOfDot == -1)
        {
            return false;
        }
        String type = StrUtil.sub(filePath, lastIndexOfDot + 1, filePath.length());

        return StrUtil.equalsAnyIgnoreCase(type, Constants.ImgType.HEIC, Constants.ImgType.JPG, Constants.ImgType.JPEG);
    }
}

package com.example.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;

public class AlarmClockTest {

    private AndroidDriver<MobileElement> driver;

    @Before
    public void setUp() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        caps.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator");
        caps.setCapability(MobileCapabilityType.APP, "/path/to/alarmclock.apk");  // Đường dẫn đến file APK của ứng dụng

        driver = new AndroidDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), caps);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @Test
    public void testSetAlarm() throws InterruptedException {
        // Chọn giờ và phút trên TimePicker
        MobileElement timePickerHour = driver.findElement(By.id("timePicker"));
        timePickerHour.sendKeys("6"); // Đặt giờ báo thức

        MobileElement timePickerMinute = driver.findElement(By.id("timePicker"));
        timePickerMinute.sendKeys("30"); // Đặt phút báo thức

        // Nhấn vào nút "Đặt báo thức"
        MobileElement btnSetAlarm = driver.findElement(By.id("btnSetAlarm"));
        btnSetAlarm.click();

        // Chờ cho đến khi báo thức được kích hoạt
        Thread.sleep(5000); // Có thể điều chỉnh thời gian chờ

        // Kiểm tra nếu thông báo báo thức xuất hiện
        MobileElement notification = driver.findElement(By.xpath("//android.widget.TextView[@text='Báo thức']"));
        assert(notification.isDisplayed());

        // Hoãn báo thức (Snooze)
        MobileElement btnSnooze = driver.findElement(By.xpath("//android.widget.Button[@text='Hoãn']"));
        btnSnooze.click();

        // Chờ thêm 5 phút để kiểm tra xem báo thức có được kích hoạt lại không
        Thread.sleep(300000); // 5 phút

        // Kiểm tra lại nếu báo thức được kích hoạt sau khi hoãn
        MobileElement notificationAfterSnooze = driver.findElement(By.xpath("//android.widget.TextView[@text='Báo thức']"));
        assert(notificationAfterSnooze.isDisplayed());

        // Tắt báo thức
        MobileElement btnStopAlarm = driver.findElement(By.xpath("//android.widget.Button[@text='Tắt']"));
        btnStopAlarm.click();
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

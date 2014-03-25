package ca.taglab.vocabnomad;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        mailTo = "vocabnomad@taglab.ca",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.SILENT
)
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}

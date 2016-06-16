package sturmtruppen.com.trafficwidget;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import junit.framework.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * The configuration screen for the {@link TrafficWidget TrafficWidget} AppWidget.
 */
public class TrafficWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "sturmtruppen.com.trafficwidget.TrafficWidget";
    private static final String PREF_PREFIX_KEY = "TW_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    EditText mFrom;
    EditText mTo;
    EditText mWarning;
    EditText mAlert;
    EditText mTimeReverse;

    Calendar dateAndTime = Calendar.getInstance();
    SimpleDateFormat fmtDateAndTime = new SimpleDateFormat("HH:mm");

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = TrafficWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String from = mFrom.getText().toString();
            String to = mTo.getText().toString();
            String warningTsd = mWarning.getText().toString();
            String alertTsd = mAlert.getText().toString();
            String timeReverse = mTimeReverse.getText().toString();
            saveDestinationPref(context, mAppWidgetId, from, to);
            saveThresholdPref(context, mAppWidgetId, warningTsd, alertTsd);
            saveTimeReversePref(context, mAppWidgetId, timeReverse);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            TrafficWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);


            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };


    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay,
                              int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            mTimeReverse.setText(fmtDateAndTime.format(dateAndTime.getTime()));
        }
    };

    public TrafficWidgetConfigureActivity() {
        super();
    }

    /**
     * Metodo per salvare l'orario di inversione percorso nelle SharedPreference
     *
     * @param context
     * @param appWidgetId
     * @param time
     */
    static void saveTimeReversePref(Context context, int appWidgetId, String time) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + "timeReverse", time);
        prefs.apply();
    }

    /**
     * Metodo per salvare le destinazioni nelle SharedPreference
     *
     * @param context
     * @param appWidgetId
     * @param from
     * @param to
     */
    static void saveDestinationPref(Context context, int appWidgetId, String from, String to) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + "from", from);
        prefs.putString(PREF_PREFIX_KEY + "to", to);
        prefs.apply();
    }

    /**
     * Metodo per salvare le soglie utilizzate per stabilire il colore del widget
     * @param context
     * @param appWidgetId
     * @param warning
     * @param alert
     */
    static void saveThresholdPref(Context context, int appWidgetId, String warning, String alert) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + "warning", warning);
        prefs.putString(PREF_PREFIX_KEY + "alert", alert);
        prefs.apply();
    }

    /**
     * Metodo per caricare la località di partenza dalle SharedPreference
     * @param context
     * @param appWidgetId
     * @return
     */
    static String loadFromPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String fromValue = prefs.getString(PREF_PREFIX_KEY + "from", null);
        if (fromValue != null) {
            return fromValue;
        } else {
            return "dummyFrom";
        }
    }

    /**
     * Metodo per caricare la località di arrivo dalle SharedPreference
     * @param context
     * @param appWidgetId
     * @return
     */
    static String loadToPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String toValue = prefs.getString(PREF_PREFIX_KEY + "to", null);
        if (toValue != null) {
            return toValue;
        } else {
            return "dummyTo";
        }
    }

    /**
     * Metodo per caricare il valore della soglia di warning
     *
     * @param context
     * @param appWidgetId
     * @return
     */
    static String loadWarningPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String toValue = prefs.getString(PREF_PREFIX_KEY + "warning", null);
        if (toValue != null) {
            return toValue;
        } else {
            return "999";
        }
    }

    /**
     * Metodo per caricare il valore della soglia di alert
     *
     * @param context
     * @param appWidgetId
     * @return
     */
    static String loadAlertPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String toValue = prefs.getString(PREF_PREFIX_KEY + "alert", null);
        if (toValue != null) {
            return toValue;
        } else {
            return "999";
        }
    }

    static String loadTimeReversePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String timeValue = prefs.getString(PREF_PREFIX_KEY + "timeReverse", null);
        if (timeValue != null) {
            return timeValue;
        } else {
            return "23:59";
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.traffic_widget_configure);
        //mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        //--
        mFrom = (EditText) findViewById(R.id.txtFrom);
        mTo = (EditText) findViewById(R.id.txtTo);
        mWarning = (EditText) findViewById(R.id.txtWarning);
        mAlert = (EditText) findViewById(R.id.txtAlert);
        mTimeReverse = (EditText) findViewById(R.id.txtTimeReverse);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);
        findViewById(R.id.txtTimeReverse).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new TimePickerDialog(TrafficWidgetConfigureActivity.this,
                        t,
                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                        dateAndTime.get(Calendar.MINUTE),
                        true).show();
            }
        });

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        //mAppWidgetText.setText(loadTitlePref(TrafficWidgetConfigureActivity.this, mAppWidgetId));
        //--
        mFrom.setText(loadFromPref(TrafficWidgetConfigureActivity.this, mAppWidgetId));
        mTo.setText(loadToPref(TrafficWidgetConfigureActivity.this, mAppWidgetId));
        mWarning.setText(loadWarningPref(TrafficWidgetConfigureActivity.this, mAppWidgetId));
        mAlert.setText(loadAlertPref(TrafficWidgetConfigureActivity.this, mAppWidgetId));
        mTimeReverse.setText(loadTimeReversePref(TrafficWidgetConfigureActivity.this, mAppWidgetId));
    }
}


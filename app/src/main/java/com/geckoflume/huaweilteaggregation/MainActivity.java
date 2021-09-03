package com.geckoflume.huaweilteaggregation;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class MainActivity extends Activity implements StatusCallback {
    public static final String TAG = "MainActivity";
    public static final String ip = "192.168.8.1";
    public static final String pwd = "hv6PKSbgh";
    public static final String band1 = "8000000";
    public static final String band2 = "8000044";

    private Button button;
    private ProgressBar huaweiProgressBar;
    private TextView speedTextView;
    private TextView speedDescTextView;
    private ProgressBar speedtestProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.button = findViewById(R.id.button);
        this.huaweiProgressBar = findViewById(R.id.progressBar);
        this.speedTextView = findViewById(R.id.speed);
        this.speedDescTextView = findViewById(R.id.speedDesc);
        this.speedtestProgressBar = findViewById(R.id.speedtestProgressBar);
        this.speedtestProgressBar.setProgress(0);

        updateLoginTextView(false, true);
        updateBandTextView(band1, 1, false, true);
        updateBandTextView(band2, 2, false, true);

        // Allow network on main thread, oops
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SpeedTestSocket speedTestSocket = new SpeedTestSocket();
        // Add a listener to wait for speedtest completion and progress
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
            @Override
            public void onCompletion(final SpeedTestReport report) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float speed = report.getTransferRateBit().divide(new BigDecimal(1000000), 2, RoundingMode.HALF_UP).floatValue();
                        if (speed > 15) {
                            speedtestProgressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                            speedDescTextView.setText(getString(R.string.speedDescFormat, "suffisante (>15 Mbps), réinitialisation inutile"));
                        }
                        else if (speed > 10) {
                            speedtestProgressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                            speedDescTextView.setText(getString(R.string.speedDescFormat, "limite (>10 Mbps), réinitialisation possible"));
                        }
                        else {
                            speedtestProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                            speedDescTextView.setText(getString(R.string.speedDescFormat, "insuffisante, réinitialisation nécessaire"));
                        }
                        speedtestProgressBar.setProgress(100);
                        speedTextView.setText(getString(R.string.speedFormat, speed));
                        button.setEnabled(true);
                    }
                });
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                // called when a download/upload error occur
            }

            @Override
            public void onProgress(final float percent, final SpeedTestReport report) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float speed = report.getTransferRateBit().divide(new BigDecimal(1000000), 2, RoundingMode.HALF_UP).floatValue();
                        if (speed > 18)
                            speedtestProgressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                        else if (speed > 10)
                            speedtestProgressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                        else
                            speedtestProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                        speedtestProgressBar.setProgress((int) percent);
                        speedTextView.setText(getString(R.string.speedFormat, speed));
                    }
                });
            }
        });
        speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/10M.iso");
    }

    public void resetConnection(View view) {
        this.button.setEnabled(false);
        this.huaweiProgressBar.setProgress(0);
        final HuaweiRequest request = new HuaweiRequest(this, pwd, ip);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                request.fetchCsrf();
                huaweiProgressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        huaweiProgressBar.setProgress(17);
                    }
                });

                Runnable runnable2 = new Runnable() {
                    @Override
                    public void run() {
                        if (request.login()) {
                            huaweiProgressBar.post(new Runnable() {
                                @Override
                                public void run() {
                                    huaweiProgressBar.setProgress(34);
                                }
                            });
                            Runnable runnable3 = new Runnable() {
                                @Override
                                public void run() {
                                    request.getSesTokInfo();
                                    huaweiProgressBar.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            huaweiProgressBar.setProgress(50);
                                        }
                                    });
                                    Runnable runnable4 = new Runnable() {
                                        @Override
                                        public void run() {
                                            if (request.changeBand(band1, 1)) {
                                                huaweiProgressBar.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        huaweiProgressBar.setProgress(66);
                                                    }
                                                });
                                                Runnable runnable5 = new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        request.getSesTokInfo();
                                                        huaweiProgressBar.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                huaweiProgressBar.setProgress(82);
                                                            }
                                                        });
                                                        Runnable runnable6 = new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (request.changeBand(band2, 2)) {
                                                                    huaweiProgressBar.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            huaweiProgressBar.setProgress(100);
                                                                            button.setEnabled(true);
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        };
                                                        new Thread(runnable6).start();
                                                    }
                                                };
                                                new Thread(runnable5).start();
                                            }
                                        }
                                    };
                                    new Thread(runnable4).start();
                                }
                            };
                            new Thread(runnable3).start();
                        }
                    }
                };
                new Thread(runnable2).start();
            }
        };
        new Thread(runnable).start();
        /*
        request.fetchCsrf();
        this.huaweiProgressBar.setProgress(17);
        if (request.login()) {
            this.huaweiProgressBar.setProgress(34);
            request.getSesTokInfo();
            this.huaweiProgressBar.setProgress(50);
            if (request.changeBand(band1, 1)) {
                this.huaweiProgressBar.setProgress(66);
                request.getSesTokInfo();
                this.huaweiProgressBar.setProgress(82);
                request.changeBand(band2, 2);
                this.huaweiProgressBar.setProgress(100);
            }
        }
        */
    }

    @Override
    public void updateLoginTextView(boolean status, boolean init) {
        TextView tv = findViewById(R.id.loginTextView);
        if (init) {
            tv.setText(getString(R.string.loginFormat, ""));
            tv.setTextColor(Color.WHITE);
        } else {
            tv.setText(getString(R.string.loginFormat, status ? "OK" : "KO"));
            tv.setTextColor(status ? Color.GREEN : Color.RED);
        }
    }

    @Override
    public void updateBandTextView(String band, int bandNumber, boolean status, boolean init) {
        TextView tv;
        String text;
        if (bandNumber == 1)
            tv = findViewById(R.id.band1TextView);
        else
            tv = findViewById(R.id.band2TextView);

        if (init) {
            text = getString(R.string.freqChangeFormat,
                    band,
                    bandNumber == 1 ? "UL" : "DL",
                    band.equals(band1) ? "B20" : "B28+B7+B3",
                    "");
            tv.setTextColor(Color.WHITE);
        } else {
            text = getString(R.string.freqChangeFormat,
                    band,
                    bandNumber == 1 ? "UL" : "DL",
                    band.equals(band1) ? "B20" : "B28+B7+B3",
                    status ? "OK" : "KO");
            tv.setTextColor(status ? Color.GREEN : Color.RED);
        }
        tv.setText(text);
    }
}
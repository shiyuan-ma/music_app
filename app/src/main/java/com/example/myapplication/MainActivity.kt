package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        val webSettings = webView.settings
        
        // Enable essential web features
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false // Attempt to allow background audio
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // Allow mixed content

        // Improve layout, scaling, and user interaction
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        // Set clients for handling rendering, navigation, and special behaviors
        webView.webChromeClient = object : WebChromeClient() {
            // Handle file chooser
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE)
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url != null && "mashiyuan.top:10000" in url) {
                    val javascript = """
                    (function() {
                        function applyStyling() {
                            if (document.getElementById('gemini-injected-styles-v2')) { return; }
                            const style = document.createElement('style');
                            style.id = 'gemini-injected-styles-v2';
                            style.innerHTML = `
                                #filelist tr {
                                    display: flex !important;
                                    justify-content: space-between !important;
                                    align-items: center !important;
                                    width: 100%;
                                }
                                #filelist tr > td { padding: 8px !important; }

                                /* Reorder columns using flexbox 'order' */
                                /* 1. Actions */
                                #filelist tr > td:nth-child(4) {
                                    order: 1;
                                    flex-shrink: 0;
                                    width: 120px;
                                    text-align: left;
                                }
                                /* 2. Name */
                                #filelist tr > td:nth-child(1) {
                                    order: 2;
                                    flex-grow: 1;
                                    min-width: 0;
                                    white-space: nowrap !important;
                                    overflow: hidden !important;
                                    text-overflow: ellipsis !important;
                                }
                                /* 3. Size */
                                #filelist tr > td:nth-child(2) {
                                    order: 3;
                                    flex-shrink: 0;
                                    width: 100px;
                                    text-align: right;
                                }
                                /* 4. Time */
                                #filelist tr > td:nth-child(3) {
                                    order: 4;
                                    flex-shrink: 0;
                                    width: 180px;
                                    text-align: right;
                                }
                            `;
                            document.head.appendChild(style);
                        }

                        let attempts = 0;
                        const interval = setInterval(function() {
                            attempts++;
                            if (document.querySelector('#filelist tr') || attempts > 20) {
                                applyStyling();
                                clearInterval(interval);
                            }
                        }, 250);
                    })();
                    """
                    view?.evaluateJavascript(javascript, null)
                }

                // Add a top padding to the body to push all content down
                val finalJs = """
                (function() {
                    document.body.style.paddingTop = '60px';
                })();
                """
                view?.evaluateJavascript(finalJs, null)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && "music.gdstudio.xyz" in url) {
                    // Intercept the music player URL and launch our native PlayerActivity
                    val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                    startActivity(intent)
                    return true // Indicate that we have handled the URL
                }

                // For all other URLs, let the WebView handle it
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
        
        webView.loadUrl("https://www.mashiyuan.top/")
    }

    // Handle result from file chooser
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (filePathCallback != null) {
                val results = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                filePathCallback!!.onReceiveValue(results)
                filePathCallback = null
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Handle back button press to navigate WebView history
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
        else {
            super.onBackPressed()
        }
    }
}
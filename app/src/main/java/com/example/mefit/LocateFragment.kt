package com.example.mefit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView


class LocateFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_locate, container, false)

        val webView: WebView = view.findViewById(R.id.webview)

        // Enable JavaScript
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        // Load HTML string
        val customHtml = """
            <!DOCTYPE html>
            <html>
            <body>
            
            <iframe src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d76099.97914285323!2d-3.145461387778569!3d53.41260225133409!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x487b212487d2f331%3A0x253438e527805162!2sPureGym%20Liverpool%20Central!5e0!3m2!1sen!2sin!4v1692727414230!5m2!1sen!2sin" width="600" height="1000" style="border:0;" allowfullscreen="" loading="lazy" referrerpolicy="no-referrer-when-downgrade"></iframe>
            
            </body>
            </html>
        """.trimIndent()
        webView.loadData(customHtml, "text/html", "UTF-8")

        return view
    }


}
package com.example.android.KingsNews.utilities;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JsonParser {

    public static String[] parserJSON(Context context,String newsJSON) throws JSONException{

        String[] parsedData = null;


        final String OWN_LIST="articles";
        String source,author,title,description,content,url;

        JSONObject baseJson=new JSONObject(newsJSON);
        JSONArray articleArray=baseJson.getJSONArray(OWN_LIST);

        parsedData=new String[articleArray.length()];

        for (int i=0;i<articleArray.length();i++) {

            JSONObject aArticle = articleArray.getJSONObject(i);


            JSONObject src=aArticle.getJSONObject("source");
            source=src.getString("name");
            title=aArticle.getString("title");
            author=aArticle.getString("author");
            description=aArticle.getString("description");
            content=aArticle.getString("content");
            url=aArticle.getString("url");

            parsedData[i]="Source: "+source+"\n\nTitle: "+title+"\n\nAuthor: "+author+"\n\nDescription "+description+"\n\nContent: "+content+"\n\nUrl: "+url+"\n\n";

        }
        return parsedData;
    }


}

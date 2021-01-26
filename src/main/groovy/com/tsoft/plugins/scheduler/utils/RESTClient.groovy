package com.tsoft.plugins.scheduler.utils

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

import java.util.logging.Logger

class RESTClient {

    private static Logger log = Logger.getLogger(RESTClient.class.getName())
    private MediaType mediaType = MediaType.get("application/json; charset=utf-8")
    private OkHttpClient client = null
    private Request request = null
    private Request.Builder builder = null

    RESTClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .followRedirects(false)
                .build()

        this.builder = new Request.Builder()
    }

    Map post(String json) throws IOException {
        RequestBody body = RequestBody.create(json, mediaType)
        this.request = builder.post(body).build()
        return newCall(request)
    }

    Map put(String json) throws IOException {
        RequestBody body = RequestBody.create(json, mediaType)
        this.request = builder.put(body).build()
        return newCall(request)
    }

    Map delete(String json) throws IOException {
        RequestBody body = RequestBody.create(json, mediaType)
        this.request = builder.delete(body).build()
        return newCall(request)
    }

    Map get() throws IOException {
        this.request = builder.get().build()
        return newCall(request)
    }

    Map upload(String url, File file) throws IOException {
        this.upload(url, file, [:])
    }

    Map upload(String url, File file, LinkedHashMap dataPart) throws IOException {
        MultipartBody.Builder multipart_builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("text/plain")))
        dataPart.each { k,v ->
            multipart_builder.addFormDataPart(k, v)
        }
        RequestBody formBody = multipart_builder.build()
        this.request = builder.url(url).post(formBody).build()
        return newCall(request)
    }

    private Map<?,?> newCall(Request req){
        try {
            Response response = client.newCall(req).execute()
            def result = [code: response.code(), message: response.message(), body: response.body().string()]
            response.body().close()
            return result
        }
        catch (ex){
            log.severe(ex.toString())
            throw ex
        }
    }

    boolean downloadTo(String pathToDowload) throws IOException {
        return downloadTo(new File(pathToDowload))
    }

    boolean downloadTo(File pathToDowload) throws IOException {
        this.request = builder.build()
        InputStream inputStream = null
        OutputStream output = null
        try {
            Response response = client.newCall(request).execute()
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response)

            byte[] buff = new byte[1024 * 4]
            long downloaded = 0
            long target = response.body().contentLength()
            inputStream = response.body().byteStream()
            output = new FileOutputStream(pathToDowload)

            while (true) {
                int readed = inputStream.read(buff)
                if (readed == -1) { break }
                output.write(buff, 0, readed)
                downloaded += readed
            }
            response.body().close()
            return downloaded == target
        }
        catch (ex){
            log.severe(ex.toString())
            throw ex
        }
        finally {
            if( inputStream!=null )
                inputStream.close()
            if( output != null ){
                output.flush()
                output.close()
            }
        }
    }

    RESTClient setUrl(String url){
        this.builder.url(url)
        return this
    }

    RESTClient addHeader(String header, String value){
        if( this.request!=null && this.request.header(header)!=null)
            this.builder.header(header, value)
        else
            this.builder.addHeader(header, value)
        return this
    }

    RESTClient setMediaType(String mediaType){
        this.setMediaType(mediaType, "utf-8")
    }

    RESTClient setMediaType(String mediaType, String charset){
        this.mediaType = MediaType.get("${mediaType}; charset=${charset}")
        return this
    }
}

package com.peihua8858.logfileserver.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * 日志控制台
 */
@Controller
@RequestMapping("/")
class MainController @Autowired constructor() {


//    /**
//     * Android接口模拟
//     *
//     * @param request
//     * @return
//     * @ignoreParams request
//     */
//    //@RequestMapping({"/{path:[^\\.]*}", "/**/{path:^(?!oauth).*}/{path:[^\\.]*}"})
//    @RequestMapping("api_android/**")
//    @ResponseBody
//    fun androidApiModel(request: HttpServletRequest): String? {
//        val result = queryUrlByModel(request)
//        if (result != null) {
//            return result
//        }
//        return JSON.toJSONString(Response.failed(HttpStatus.NOT_FOUND, "Interface not found."))
//    }

//    /**
//     * iOS接口模拟
//     *
//     * @param request
//     * @return
//     * @ignoreParams request
//     */
//    @RequestMapping("api_ios/**")
//    @ResponseBody
//    fun iOSApiModel(request: HttpServletRequest): String? {
//        val result = queryUrlByModel(request)
//        if (result != null) {
//            return result
//        }
//        return JSON.toJSONString(Response.failed(HttpStatus.NOT_FOUND, "Interface not found."))
//    }

//    private fun queryUrlByModel(request: HttpServletRequest): String? {
//        try {
//            val url = request.getRequestURL().toString()
//            if (!url.contains("favicon.ico")) {
//                val path = URI.create(url).getRawPath()
//                LOG.info("url>>>" + url)
//                LOG.info("path>>>" + path)
//                val apiModel: ApiModel? = this.apiModelService.selectByUrl(path)
//                LOG.info("apiModel>>>" + apiModel)
//                if (apiModel != null) {
//                    return apiModel.getResponse()
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return null
//    }

    /**
     * 接口模拟首页列表
     * 
     * @return
     * @ignoreParams model
     * @ignoreParams device
     */
    @GetMapping
    fun findList( model: Model?): String {
            return "logcat/logconsole"
    }

    //    @GetMapping("upload")
    //    public String uploadfile(Device device, Model model) {
    //        return "/uploadFile/index2";
    //    }
    /**
     * 进入工具页
     * 
     * @return
     * @ignoreParams model
     * @ignoreParams device
     */
    @GetMapping("tools")
    fun tools(model: Model?): String {
        return "/tools/index"
    }


    /**
     * 进入deeplink页面
     * 
     * @return
     * @ignoreParams model
     * @ignoreParams device
     */
    @GetMapping("links")
    fun links( model: Model?): String {
        return "redirect:link/zaful"
    }

    //    /**
    //     * 进入ZF deeplink页面
    //     *
    //     * @return
    //     * @ignoreParams model
    //     * @ignoreParams device
    //     */
    //    @GetMapping("zflink")
    //    public String zfLinks(Device device, Model model) {
    //        return "/pc/links/zflink";
    //    }
    //
    //    /**
    //     * 进入rg deeplink页面
    //     *
    //     * @return
    //     * @ignoreParams model
    //     * @ignoreParams device
    //     */
    //    @GetMapping("rglink")
    //    public String rgLinks(Device device, Model model) {
    //        return "/pc/links/rglink";
    //    }
    //
    //    /**
    //     * 进入DL deeplink页面
    //     *
    //     * @return
    //     * @ignoreParams model
    //     * @ignoreParams device
    //     */
    //    @GetMapping("dllink")
    //    public String dlLinks(Device device, Model model) {
    //        return "/pc/links/dllink";
    //    }
    /**
     * 进入日志控制台
     * 
     * @return
     * @ignoreParams model
     * @ignoreParams device
     */
    @GetMapping("logcat")
    fun logcat(model: Model?): String {
            return "logcat/logconsole"
    }
    

//    /**
//     * 进入统计日志列表
//     *
//     * @return
//     * @ignoreParams model
//     * @ignoreParams device
//     */
//    @PostMapping("eventinfo")
//    @ResponseBody
//    fun eventinfo(@RequestBody params: MutableMap<String?, Any?>?): String {
//        LOG.info(Gson().toJson(params))
//        val uuid = UUID.randomUUID()
//        var requestId = uuid.leastSignificantBits
//        requestId = abs(requestId)
//        return "{\"requestid\":\"$requestId\",\"result_code\":200,\"result_msg\":\"success\"}"
//    }

    /**
     * 进图片列表
     * 
     * @return
     * @ignoreParams model
     * @ignoreParams device
     */
    @GetMapping("showImages")
    fun imageShow(): String {
        return "/image_show"
    }

//    /**
//     * 查询所有图片
//     *
//     * @return
//     * @ignoreParams model
//     * @ignoreParams device
//     */
//    @GetMapping("queryAllImages")
//    @ResponseBody
//    fun queryAllImages(): Callable<Response<MutableList<FolderMenu?>?>?> {
//        return object : Callable<Response<MutableList<FolderMenu?>?>?> {
//            @Throws(Exception::class)
//            override fun call(): Response<MutableList<FolderMenu?>?> {
//                val parentFile: File = ServiceApplication.readJarFolder()
//                LOG.info("path:" + parentFile.getAbsolutePath())
//                val imageFolder = StringBuilder()
//                imageFolder.append("upload").append(File.separator).append("images").append(File.separator)
//                val imageFolderFile = File(parentFile, imageFolder.toString())
//                if (!imageFolderFile.exists()) {
//                    return Response.ok()
//                }
//                val data: MutableList<FolderMenu?> = ArrayList<FolderMenu?>()
//                findChildMenu(data, true, imageFolderFile, "", imageFolder.toString())
//                return Response.ok(data)
//            }
//        }
//    }

//    /**
//     * 根据条件查询图片
//     *
//     * @param appName  项目名称
//     * @param platform 系统平台，Android或iOS
//     * @param version  版本号
//     * @return
//     */
//    @PostMapping("queryImages")
//    @ResponseBody
//    fun queryImages(
//        @RequestParam(value = "appName", defaultValue = "") appName: String?,
//        @RequestParam(value = "platform", defaultValue = "") platform: String?,
//        @RequestParam(value = "version", defaultValue = "") version: String?
//    ): Callable<Response<MutableList<FileModel?>?>?> {
//        return object : Callable<Response<MutableList<FileModel?>?>?> {
//            @Throws(Exception::class)
//            override fun call(): Response {
//                val parentFile: File = ServiceApplication.readJarFolder()
//                LOG.info("path:" + parentFile.getAbsolutePath())
//                val imageFolder = StringBuilder()
//                imageFolder.append("upload").append(File.separator).append("images").append(File.separator)
//                val imageFolderFile = File(parentFile, imageFolder.toString())
//                if (!imageFolderFile.exists()) {
//                    return Response.ok()
//                }
//                //如果上传目录为/files/upload/images/zaful/7.2.3/[android/ios]/，则可以如下获取：
//                var includePattern =
//                    (if (StringUtils.isEmpty(appName)) "*" else appName!!.lowercase(Locale.getDefault())) + File.separator
//                includePattern += (if (StringUtils.isEmpty(version)) "*" else version!!.lowercase(Locale.getDefault())) + File.separator
//                includePattern += (if (StringUtils.isEmpty(platform)) "*" else platform!!.lowercase(Locale.getDefault())) + File.separator
//                println(includePattern)
//                val result: MutableList<FileModel?> =
//                    findFileModel(imageFolderFile, includePattern, imageFolder.toString(), false)
//                return Response.ok(result)
//            }
//        }
//    }

//    /**
//     * 上传图片
//     *
//     * @param appName  项目名称
//     * @param platform 系统平台，Android或iOS
//     * @param version  版本名称
//     * @param files    文件列表
//     * @return
//     */
//    @PostMapping("uploadImage")
//    @ResponseBody
//    @Deprecated("")
//    fun uploadImage(
//        @RequestParam("appName") appName: String,
//        @RequestParam("platform") platform: String,
//        @RequestParam("version") version: String,
//        @RequestParam("files") files: Array<MultipartFile>?
//    ): Response<MutableList<EntityBean?>?> {
//        if (files == null || files.size == 0 || Utils.isEmptyException(appName, version, platform)) {
//            return Response.failed("Multiple request parameters are invalid.")
//        }
//        val parentFile: File = ServiceApplication.readJarFolder()
//        LOG.info("path:" + parentFile.getAbsolutePath())
//        //如果上传目录为/files/upload/images/zaful/7.2.3/[android/ios]/，则可以如下获取：
//        val uploadFolder =
//            "upload" + File.separator + "images" + File.separator + createFolderName() + File.separator + appName.lowercase(
//                Locale.getDefault()
//            ) + File.separator + version.lowercase(Locale.getDefault()) + File.separator + platform.lowercase(
//                Locale.getDefault()
//            ) + File.separator
//
//        val uploadFolderFile = File(parentFile, uploadFolder)
//        if (!uploadFolderFile.exists()) {
//            uploadFolderFile.mkdirs()
//        }
//        LOG.info("parentFile:" + uploadFolderFile.getAbsolutePath())
//        try {
//            val data: MutableList<EntityBean?> = ArrayList<EntityBean?>()
//            for (file in files) {
//                val contentType: String? = file.getContentType()
//                if (contentType == null) {
//                    data.add(EntityBean(file.getName(), "Content-Type is null."))
//                    continue
//                }
//                val originalFilename: String? = file.getOriginalFilename()
//                val image: Image? = ImageIO.read(file.getInputStream())
//                if (contentType.startsWith("image/") || image != null) {
//                    try {
//                        val imageFile = File(uploadFolderFile, createFileName(originalFilename))
//                        LOG.info("imageFile:" + imageFile.getAbsolutePath())
//                        LOG.info("imageFile.getParentFile():" + imageFile.getAbsoluteFile())
//                        file.transferTo(imageFile) //保存文件
//                        val backFilePath = "files" + File.separator + uploadFolder + imageFile.getName()
//                        data.add(EntityBean(file.getName(), backFilePath))
//                        LOG.info("backIconPath:" + backFilePath)
//                    } catch (e: Exception) {
//                        LOG.info("backIconPath:" + XUtils.getStackTraceMessage(e))
//                        e.printStackTrace()
//                        data.add(EntityBean(file.getName(), e.message))
//                    }
//                } else {
//                    data.add(EntityBean(file.getName(), "File is not support."))
//                }
//            }
//            return Response.ok(data).setMsg("Picture uploaded successfully.")
//        } catch (e: IOException) {
//            e.printStackTrace()
//            LOG.error(XUtils.getStackTraceMessage(e))
//            return Response.failed(e.message)
//        }
//    }
//
//    //修改生成的apk名字Demo_v1.0.0_1_deploy_1509051415.apk ,
//    // 规则:[app_name]_[version_name]_[version_code]_[deploy|develop]_[date].apk
//    @Deprecated("")
//    fun createFileName(oriFileName: String?): String {
//        val extensionName: String? = FilenameUtils.getExtension(oriFileName)
//        val timeStamp = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(Date())
//        return "IMG_" + timeStamp + "." + extensionName
//    }

    @Deprecated("")
    fun createFolderName(): String {
        val folderName = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return folderName
    }


    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(MainController::class.java)
//        private fun findChildMenu(
//            data: MutableList<FolderMenu?>, isChecked: Boolean, parentFile: File,
//            includePattern: String?, imageFolder: String?
//        ) {
//            val files = parentFile.listFiles()
//            if (files != null && files.size > 0) {
//                Arrays.sort<File?>(files, Comparator { file1: File?, file2: File? ->
//                    val fileName1 = file1!!.getName()
//                    val fileName2 = file2!!.getName()
//                    fileName2.compareTo(fileName1)
//                })
//                var index = 0
//                for (file in files) {
//                    if (file.isDirectory()) {
//                        val menu: FolderMenu = FolderMenu()
//                        val result: MutableList<FileModel?> = findFileModel(file, includePattern, imageFolder, true)
//                        menu.setFiles(result)
//                        menu.setHref("")
//                        val state: MutableMap<String?, Boolean?> = HashMap<String?, Boolean?>()
//                        state.put("selected", index == 0 && isChecked)
//                        state.put("expanded", index == 0 && isChecked)
//                        menu.setState(state)
//                        menu.setText(file.getName())
//                        val childNodes: MutableList<FolderMenu?> = ArrayList<FolderMenu?>()
//                        findChildMenu(
//                            childNodes, false, file, includePattern,
//                            imageFolder + file.getName() + File.separator
//                        )
//                        menu.setNodes(childNodes)
//                        menu.setTags(mutableListOf<T?>(ParseUtil.toString(result.size)))
//                        data.add(menu)
//                        index++
//                    }
//                }
//            }
//        }
//
//        private fun findFileModel(
//            parentFile: File, includePattern: String?, imageFolder: String?,
//            isAddParentName: Boolean
//        ): MutableList<FileModel?> {
//            val result: MutableList<FileModel?> = ArrayList<FileModel?>()
//            val files: MutableList<String> = FileScanUtils.scanFiles(parentFile, includePattern)
//            Collections.sort<String?>(files, Comparator { path1: String?, path2: String? ->
//                val file1 = File(parentFile, path1)
//                val file2 = File(parentFile, path2)
//                val file1Time = file1.lastModified()
//                val file2Time = file2.lastModified()
//                file2Time.compareTo(file1Time)
//            })
//            var imageParentPath = "files" + File.separator + imageFolder + File.separator
//            if (isAddParentName) {
//                imageParentPath += parentFile.getName() + File.separator
//            }
//            for (file in files) {
//                val index = 0
//                val model: FileModel = FileModel()
//                val childFile = File(parentFile, file)
//                model.setCreateTime(SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date(childFile.lastModified())))
//                setModel(index, model, childFile)
//                model.setFileName(childFile.getName())
//                model.setFilePath(imageParentPath + file)
//                result.add(model)
//            }
//            return result
//        }

        //    public static void main(String[] args) {
        //        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(new Date());
        //        System.out.println(timeStamp);
        //        String appName = "";
        //        String platform = "";
        //        String version = "";
        //        String jarPath = "D:\\WorkSpace\\Project\\JavaEE\\LogService\\log-module\\target";
        //        StringBuilder imageFolder = new StringBuilder();
        //        imageFolder.append("upload").append(File.separator).append("images").append(File.separator);
        //        File parentFile = new File(jarPath, imageFolder.toString());
        //        if (!parentFile.exists()) {
        //            return;
        //        }
        //        //如果上传目录为/files/upload/images/zaful/7.2.3/[android/ios]/，则可以如下获取：
        //        String includePattern = (StringUtils.isEmpty(appName) ? "*" : appName.toLowerCase()) + File.separator;
        //        includePattern += (StringUtils.isEmpty(version) ? "*" : version.toLowerCase()) + File.separator;
        //        includePattern += (StringUtils.isEmpty(platform) ? "*" : platform.toLowerCase()) + File.separator;
        //        System.out.println(includePattern);
        //        List<String> files = FileScanUtils.scanFiles(parentFile, includePattern);
        //        System.out.println("排序前>>>" + files);
        //        Collections.sort(files, (path1, path2) -> {
        //            File file1 = new File(parentFile, path1);
        //            File file2 = new File(parentFile, path2);
        //            Long file1Time = file1.lastModified();
        //            Long file2Time = file2.lastModified();
        //            return file2Time.compareTo(file1Time);
        //        });
        //        System.out.println("排序后>>>" + files);
        //        List<FileModel> result = new ArrayList<>();
        //        String imageParentPath = "files" + File.separator + imageFolder.toString() + File.separator;
        //        for (String file : files) {
        //            int index = 0;
        //            final FileModel model = new FileModel();
        //            File childFile = new File(parentFile, file);
        //            model.setCreateTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(childFile.lastModified
        //            ())));
        //            setModel(index, model, childFile);
        //            model.setFilePath(imageParentPath + file);
        //            result.add(model);
        //            /*System.out .println(file); */ //        }
        //        for (FileModel model : result) {
        //            System.out.println(model);
        //        }
        //
        //        String jarPath = "D:\\WorkSpace\\Project\\JavaEE\\LogService\\log-module\\target";
        //        StringBuilder imageFolder = new StringBuilder();
        //        imageFolder.append("upload").append(File.separator).append("images").append(File.separator);
        //        File parentFile = new File(jarPath, imageFolder.toString());
        //        if (!parentFile.exists()) {
        //            return;
        //        }
        //        List<FolderMenu> data = new ArrayList<>();
        //        findChildMenu(data, true, parentFile, "", imageFolder.toString());
        //        for (FolderMenu model : data) {
        //            System.out.println(model);
        //        }
        //    }

//        fun setModel(index: Int, model: FileModel, file: File?) {
////        rosegal\5.3.0\android\IMG_20210524164856042.jpg
//            if (file != null) {
//                val parentFile = file.getParentFile()
//                if (parentFile != null) {
//                    if (index == 0) {
//                        model.setPlatform(parentFile.getName())
//                        setModel(index + 1, model, parentFile)
//                    } else if (index == 1) {
//                        model.setVersion(parentFile.getName())
//                        setModel(index + 1, model, parentFile)
//                    } else if (index == 2) {
//                        model.setAppName(parentFile.getName())
//                    }
//                }
//            }
//        }
    }
}

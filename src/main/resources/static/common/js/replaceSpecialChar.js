function encodeStr(str) {
    str = str.replace("-", "44F");
    str = str.replace(" ", "%NN");
    str = str.replace(" ", "%RR");
    str = str.replace("\+", "%2B");
    str = str.replace(" ", "%20");
    str = str.replace("\?", "%3F");
    str = str.replace("#", "%23");
    str = str.replace("&", "%26");
    str = str.replace("=", "%3D");
    str = str.replace("/", "%2F");
    return str;
}
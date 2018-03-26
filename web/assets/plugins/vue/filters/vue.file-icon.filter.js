// usage: {{ file.name | fileIcon }}
Vue.filter('fileIcon', function (filename) {

  var extension = (/[.]/.exec(filename)) ? /[^.]+$/.exec(filename)[0] : undefined;

  var retorno = "fa fa-file";

  switch (extension.toLowerCase()) {
    case 'tar':
    case 'tar.gz':
    case '7zip':
    case 'rar':
    case 'zip': retorno = 'fa fa-file-archive'; break;

    case 'mpeg':
    case 'aac':
    case 'ac3':
    case 'wma':
    case 'flac':
    case 'alac':
    case 'zpl':
    case 'wpl':
    case 'mp3': retorno = 'fa fa-file-audio'; break;

    case 'txt': retorno = 'fa fa-file-edit'; break;

    case 'xls':
    case 'ods':
    case 'ots':
    case 'fods':
    case 'xlt':
    case 'csv':
    case 'xlsm':
    case 'xlsx': retorno = 'fa fa-file-excel'; break;

    case 'doc':
    case 'docx':
    case 'odt':
    case 'ott':
    case 'fodt':
    case 'dot':
    case 'rtf':
    case 'docm': retorno = 'fa fa-file-word'; break;

    case 'pdf': retorno = 'fa fa-file-pdf'; break;

    case 'xvid':
    case 'wmv':
    case 'mpeg4':
    case 'mkv':
    case 'mp4': retorno = 'fa fa-file-video'; break;

    case 'ppt':
    case 'pptx':
    case 'odp': retorno = 'fa fa-file-powerpoint'; break;

    case 'jpg':
    case 'jpeg':
    case 'png':
    case 'bmp':
    case 'gif':
    case 'tif':
    case 'tiff': retorno = 'fa fa-file-image'; break;

  }

  return retorno;
});

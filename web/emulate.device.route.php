<?php


route(@$_POST['route'], @$_POST['variables']);


function route($route, $variables){


  $retorno = 'no route';
  $status_code = 404;

  try {
    $variables = json_decode(json_encode($variables));
  } catch (\Exception $e) {
      $retorno = 'no variables';
      $status_code = 404;
  }

  switch ($route) {
    case 'listResources':
      $retorno = listResources($variables);
      $status_code = 200;
      break;
    case 'upload':
      $retorno = upload($variables);
      $status_code = 200;
      break;
    case 'delete':
      $retorno = delete($variables);
      $status_code = 200;
      break;
    case 'newFolder':
      $retorno = newFolder($variables);
      $status_code = 200;
      break;
    case 'colar':
      $retorno = colar($variables);
      $status_code = 200;
      break;
    case 'rename':
      $retorno = renomear($variables);
      $status_code = 200;
      break;
  }

  http_response_code($status_code);
  exit(
    json_encode($retorno)
  );
}


function listResources($variables){

  $path = (property_exists($variables,'path') && $variables->path) ? $variables->path : '\\';
  $path = (substr($path, -1) == '\\') ? $path : $path.'\\';


  $resources = scandir($path);

  $folders = [];
  $files = [];

  foreach ($resources as $key => $value) {

    if(in_array($value, ['.','..']))
      continue;

    if(is_dir($path.$value) && is_readable($path.$value)){
      $folders[] = [
        'name'        => $value,
        'updated_at'  => date('Y-m-d H:i:s',filectime($path.$value)),
      ];

    }elseif(is_file($path.$value) && is_readable($path.$value)){

      $files[] = [
        'name'        => $value,
        'size'        => filesize($path.$value),
        'updated_at'  => date('Y-m-d H:i:s',filectime($path.$value)),
      ];

    }

  }

  return [
    'path'    => $path,
    'folders' => $folders,
    'files'   => $files,
  ];
}


function upload($variables){

  $return = [];

  for ($i=0; $i < count($_FILES['file']['name']); $i++) {

    $uploadfile = $_POST['path'] . $_FILES['file']['name'][$i];

    $return[] = [
      'name'  => $_FILES['file']['name'][$i],
      'status'=> move_uploaded_file($_FILES['file']['tmp_name'][$i], $uploadfile),
    ];

  }

  return $return;
}

function delete($variables){

  $path = (property_exists($variables,'path') && $variables->path) ? $variables->path : '\\';
  $path = (substr($path, -1) == '\\') ? $path : $path.'\\';


  if(property_exists($variables,'folders')){

    foreach ($variables->folders as $key => $value) {
      deleteDir($path . $value);
    }

  }

  if(property_exists($variables,'files')){

    foreach ($variables->files as $key => $value) {
      unlink($path . $value);
    }

  }

  return ['status' => true];

}


function newFolder($variables){

  $path = (property_exists($variables,'path') && $variables->path) ? $variables->path : '\\';
  $path = (substr($path, -1) == '\\') ? $path : $path.'\\';

  return [
    'status' => mkdir($path . $variables->folderName)
  ];

}

function colar($variables){

  if(property_exists($variables,'folders')){

    foreach ($variables->folders as $key => $value) {

      if($variables->action == 'copy'){
        xcopy($variables->origin . $value , $variables->destiny . $value);

      }elseif($variables->action == 'move'){
        rename($variables->origin . $value , $variables->destiny . $value);

      }

    }

  }

  if(property_exists($variables,'files')){

    foreach ($variables->files as $key => $value) {

      if($variables->action == 'copy'){
        copy($variables->origin . $value, $variables->destiny . $value);

      }elseif($variables->action == 'move'){
        rename($variables->origin . $value , $variables->destiny . $value);

      }

    }

  }

  return ['status' => true];

}

function renomear($variables){


  $path = (property_exists($variables,'path') && $variables->path) ? $variables->path : '\\';
  $path = (substr($path, -1) == '\\') ? $path : $path.'\\';

  rename($path . $variables->oldName, $path . $variables->newName);

  return ['status' => true];

}













function deleteDir($path) {
    return is_file($path) ?
            @unlink($path) :
            array_map(__FUNCTION__, glob($path.'/*')) == @rmdir($path);
}



function dd($var){
  echo "<pre>";
  var_dump($var);
  echo "</pre>";
  exit();
}


/**
 * Copy a file, or recursively copy a folder and its contents
 * @author      Aidan Lister <aidan@php.net>
 * @version     1.0.1
 * @link        http://aidanlister.com/2004/04/recursively-copying-directories-in-php/
 * @param       string   $source    Source path
 * @param       string   $dest      Destination path
 * @param       int      $permissions New folder creation permissions
 * @return      bool     Returns true on success, false on failure
 */
function xcopy($source, $dest, $permissions = 0755)
{
    // Check for symlinks
    if (is_link($source)) {
        return symlink(readlink($source), $dest);
    }

    // Simple copy for a file
    if (is_file($source)) {
        return copy($source, $dest);
    }

    // Make destination directory
    if (!is_dir($dest)) {
        mkdir($dest, $permissions);
    }

    // Loop through the folder
    $dir = dir($source);
    while (false !== $entry = $dir->read()) {
        // Skip pointers
        if ($entry == '.' || $entry == '..') {
            continue;
        }

        // Deep copy directories
        xcopy("$source/$entry", "$dest/$entry", $permissions);
    }

    // Clean up
    $dir->close();
    return true;
}
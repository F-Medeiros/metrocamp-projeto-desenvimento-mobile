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


function dd($var){
  echo "<pre>";
  var_dump($var);
  echo "</pre>";
  exit();
}

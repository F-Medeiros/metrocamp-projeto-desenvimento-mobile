<?php

	$uploaddir = './uploads/';

	if(!file_exists($uploaddir))
		mkdir($uploaddir);

	$uploadfile = $uploaddir . basename($_FILES['file']['name']);

	if (move_uploaded_file($_FILES['file']['tmp_name'], $uploadfile)) {
	    echo "Arquivo válido e enviado com sucesso.\n";
	} else {
	    echo "Possível ataque de upload de arquivo!\n";
	}


	function dd($var){
	  echo "<pre>";
	  var_dump($var);
	  echo "</pre>";
	  exit();
	}

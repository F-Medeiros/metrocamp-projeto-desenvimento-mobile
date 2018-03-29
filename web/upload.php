<?php

	$uploaddir = './uploads/';

	if(!file_exists($uploaddir))
		mkdir($uploaddir);


	for ($i=0; $i < count($_FILES['file']['name']); $i++) {

		$uploadfile = $uploaddir . basename($_FILES['file']['name'][$i]);

		if (move_uploaded_file($_FILES['file']['tmp_name'][$i], $uploadfile)) {
		    echo "Arquivo válido e enviado com sucesso.\n";
		} else {
		    echo "Possível ataque de upload de arquivo!\n";
		}

	}

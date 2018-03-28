<?php
	
	$uploaddir = 'C:/Users/1510030927/Downloads/metrocamp-projeto-desenvimento-mobile/upload';
	$uploadfile = $uploaddir . basename($_FILES['file']['name']);

	if (move_uploaded_file($_FILES['file']['tmp_name'], $uploadfile)) {
	    echo "Arquivo válido e enviado com sucesso.\n";
	} else {
	    echo "Possível ataque de upload de arquivo!\n";
	}
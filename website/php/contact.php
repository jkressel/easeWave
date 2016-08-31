<?php

if(!$_POST) exit;

function tommus_email_validate($email) { return filter_var($email, FILTER_VALIDATE_EMAIL) && preg_match('/@.+\./', $email); }

$name = $_POST['name']; $email = $_POST['email']; $comments = $_POST['comments'];


if(trim($name) == '') {

	exit('<div class="error_message">Bitte geben Sie Ihren Namen an.</div>');

} else if(trim($email) == '') {

	exit('<div class="error_message">Bitte geben Sie Ihre E-Mail Adresse an.</div>');

} else if(!tommus_email_validate($email)) {

	exit('<div class="error_message">Die von Ihnen eingegebene E-Mail Adresse scheint nicht gültig zu sein.</div>');

} else if(trim($comments) == '') {

	exit('<div class="error_message">Bitte geben Sie eine Nachricht ein.</div>');

} if(get_magic_quotes_gpc()) { $comments = stripslashes($comments); }


$address = 'contact@easewave.com';

$e_subject = 'easeWave Contact Form';

$e_content = $comments;

$msg = wordwrap( $e_content, 70 );

$headers = "From: $email" . "\r\n";

$headers .= "Reply-To: $email" . "\r\n";

$headers .= "MIME-Version: 1.0" . "\r\n";

$headers .= "Content-type: text/plain; charset=utf-8" . "\r\n";

$headers .= "Content-Transfer-Encoding: quoted-printable" . "\r\n";


if(mail($address, $e_subject, $msg, $headers)) {
    echo "<div id='success_page'>Ihre Nachricht wurde erfolgreich übermittelt. Ich melde mich baldmöglichst bei Ihnen, $name.</div>";
}
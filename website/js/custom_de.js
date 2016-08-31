// TODO: make all multilangual
// TODO: favs deleted after browser close/restart?

var fabShareToggled = false;
var quotes = Parse.Object.extend("Quotes");
var query = new Parse.Query(quotes);
var currentTime = new Date();
var month = getFormattedPartTime(currentTime.getMonth() + 1);
var day = getFormattedPartTime(currentTime.getDate());
var year = currentTime.getFullYear();
var today = year + "/" + month + "/" + day;
var currentDrawerPositon = 1;
var skip = 0;
var downloadedQuotes, downloadedFavs;
var initial2 = true;
var loadingmore = false;
var drawerenabled = true;

var cardtemplate = '<div class="col1-3 white grey-area-last element home"><p class="blockquotetile">/quote/</p>' +
    '<div class="grey-area last smaller clearfix bottom-area"><p class="small">/source/</p>' +
    '<div class="sharelayout"><img class="share fb" alt="Facebook" title="auf Facebook teilen" src="../img/facebook55.png">' +
    '<img class="share google" id="/id/" alt="Google+" title="auf Google+ teilen" src="../img/google1.png"><img class="share mail" alt="E-Mail" title="via E-Mail versenden" src="../img/symbol20.png">' +
    '<img class="share twitter" alt="Twitter" title="auf Twitter teilen" src="../img/twitter1.png"><img class="fav" id="fav" alt="Favorit" title="zu Favoriten hinzufügen" src="../img/stars59.png"></div></div></div>';

var loadmorecard = '<div class="clearfix col1-1 element home"><p class="loadmore" id="loadmore">mehr laden</p></div>';

var nofavscard = '<div class="col1-1 nofavscard combi white grey-area-last element"><p class="blockquotetile">Sie haben (noch) keine Favoriten gespeichert...</p>' +
    '<div class="grey-area-fullwidth last smaller clearfix bottom-area"><p class="small">Hinweis: Favoriten werden lokal im Cache Ihres Browsers gespeichert. ' +
    'Durch das löschen von Browserdaten bzw. Caches können Ihre gespeicherten Favoriten verloren gehen!</p></div></div>';

var nosearchresultscard = '<div class="col1-1 nothingfound combi white grey-area-last element"><p class="blockquotetile">Leider nichts gefunden...</p>' +
    '<div class="grey-area-fullwidth last smaller clearfix bottom-area"><p class="small">Versuchen Sie es noch einmal mit anderen Suchbegriffen.</p></div></div>';

var $toggle = document.getElementsByClassName('drawerToggle')[0];
var $menu = document.getElementsByClassName('nav')[0];
var $darker = document.getElementsByClassName('darker')[0];
var $myResults = $("#myResults");
var $myResultssource = $("#myResultssource");
var $fabShare = $("#fabShare");
var $contact_send = $("#contact_send");
var $submit_send = $("#submit_send");
var $aboutlayout = $(".about");
var $browselayout = $(".browse");
var $submit = $(".submit");
var $contactlayout = $(".contact");
var $androidapplayout = $(".androidapp");
var $chromeextensionlayout = $(".chromeextension");
var $mooddiarylayout = $(".mooddiary");
var $search_close = $('#search-close');
var $container = $('#containerisotope');
var $alpha = $('#container');
var $search_expandable = $('#search-expandable');
var $title = $('#title');
var $searchbar = $('.searchbar');
var $fab_fav_img = $('#fab_fav_img');
var $mdl_spinner = $('.center-spinner');
var $spinner_circular = $('.center-spinner svg');
var $spinner_path = $('.center-spinner circle');

Parse.initialize("uui8BnLPxppLXhfBAbdB8GhmBcrAmWjO1MWF6tBC", "mNxPus3a9taxRGFn0TZKq2up1nvKV7idBnnhvjXN");

$myResults.transition({opacity: 0, duration: 0});
$myResults.hide();
$myResultssource.transition({opacity: 0, duration: 0});
$myResultssource.hide();
$fabShare.transition({scale: 0, duration: 0});
$contact_send.transition({scale: 0, duration: 0});
$submit_send.transition({scale: 0, duration: 0});
$searchbar.transition({opacity: 0, duration: 0});
$aboutlayout.velocity("slideUp", {duration: 0});
$browselayout.velocity("fadeOut", {duration: 0});
$submit.velocity("slideUp", {duration: 0});
$contactlayout.velocity("slideUp", {duration: 0});
$androidapplayout.velocity("slideUp", {duration: 0});
$chromeextensionlayout.velocity("slideUp", {duration: 0});
$mooddiarylayout.velocity("slideUp", {duration: 0});
$searchbar.hide();
$aboutlayout.hide();
$browselayout.hide();
$submit.hide();
$contactlayout.hide();
$androidapplayout.hide();
$chromeextensionlayout.hide();
$mooddiarylayout.hide();
$search_close.hide();
$contact_send.hide();
$submit_send.hide();
$fabShare.hide();
$(".sharemenu").hide();
$(".nav__list li:nth-child(1)").css("background-color", "#d1d1d1");

todaysquote();
readCookie();

//$('.card').show();
$('#hideall').transition({opacity: 1, duration: 500});

$toggle.addEventListener('click', function (event) {
    if (drawerenabled) {
        if (fabShareToggled) {
            $fabShare.trigger("click");
        }
        $fabShare.css('cursor', 'default');
        $menu.classList.toggle('open');
        $darker.classList.toggle('open');
        $('.darker').css("z-index", 5);
        $toggle.classList.toggle('open');
        initial2 = !initial2;
        animate({
            el: ".fabBtn",
            opacity: initial2 ? 1 : 0.5,
            duration: 500
        });
    } else {
        event.preventDefault();
    }
});

$darker.addEventListener('click', function () {
    if ($menu.classList.contains("open")) {
        $menu.classList.toggle('open');
        $darker.classList.toggle('open');
        setTimeout(function () {
            $('.darker').css("z-index", 0);
        }, 400);
        $toggle.classList.toggle('open');
        initial2 = !initial2;
        animate({
            el: ".fabBtn",
            opacity: initial2 ? 1 : 0.5,
            duration: 500
        });
        $(".fabBtn").css('cursor', 'pointer');
    }
});

document.querySelector("#fabShare").addEventListener("click", function () {
    if ($('.drawerToggle').attr("class").indexOf("open") <= -1) {
        this.classList.toggle("active");
        if (fabShareToggled) {
            $fabShare.transition({rotate: '-=360deg', duration: 500, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            setTimeout(function () {
                $("#favorite").animate({
                    bottom: '0px',
                    opacity: '0'
                });
            }, 140);
            setTimeout(function () {
                $("#facebook").animate({
                    bottom: '0px',
                    opacity: '0'
                });
            }, 100);
            setTimeout(function () {
                $("#googleplus").animate({
                    bottom: '0px',
                    opacity: '0'
                });
            }, 70);
            setTimeout(function () {
                $("#mail").animate({
                    bottom: '0px',
                    opacity: '0'
                });
            }, 30);
            $("#twitter").animate({
                bottom: '0px',
                opacity: '0'
            });
            fabShareToggled = false;
        } else {
            $fabShare.transition({rotate: '+=360deg', duration: 500, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            $("#favorite").animate({
                bottom: '55px',
                opacity: '1'
            }).fadeTo(500, 1);
            setTimeout(function () {
                $("#facebook").animate({
                    bottom: '125px',
                    opacity: '1'
                }).fadeTo(500, 1);
            }, 30);
            setTimeout(function () {
                $("#googleplus").animate({
                    bottom: '195px',
                    opacity: '1'
                }).fadeTo(500, 1);
            }, 30);
            setTimeout(function () {
                $("#mail").animate({
                    bottom: '265px',
                    opacity: '1'
                }).fadeTo(500, 1);
            }, 70);
            setTimeout(function () {
                $('#twitter').animate({
                    bottom: '335px',
                    opacity: '1'
                }).fadeTo(500, 1);
            }, 100);
            fabShareToggled = true;
        }
    }
});

document.querySelector("#contact_send").addEventListener("click", function () {
    if ($('.nav').attr("class").indexOf("open") <= -1) {
        $('#submitcontact').click();
    }
});

document.querySelector("#submit_send").addEventListener("click", function () {
    if ($('.nav').attr("class").indexOf("open") <= -1) {
        $spinner_path.addClass('is-active');
        $spinner_circular.addClass('is-active');
        $mdl_spinner.show();
        $(this).attr('disabled', true);
        // send to parse
        var ContactForm = Parse.Object.extend("AppSubmits");
        var contactData = new ContactForm();

        contactData.set("suggestion", $('#quote').val());
        contactData.set("author", $('#author').val());

        contactData.save(null, {
            success: function () {
                $mdl_spinner.hide();
                $spinner_path.removeClass('is-active');
                $spinner_circular.removeClass('is-active');
                showDialog({
                    title: 'Herzlichen Dank!',
                    text: 'Ihr Vorschlag wurde erfolgreich übermittelt.',
                    positive: {
                        title: 'OK',
                        onClick: function (e) {

                        }
                    }
                });
                document.getElementById('quote').value = '';
                document.getElementById('author').value = '';
                $('#quote').parent().removeClass('is-dirty');
                $('#author').parent().removeClass('is-dirty');
                setTimeout(function () {
                    if ($('#quote').val() != '') {
                        $('#submit_send').attr('disabled', false);
                    } else {
                        $('#submit_send').attr('disabled', true);
                    }
                }, 200);
            },
            error: function (gameScore, error) {
                $mdl_spinner.hide();
                $spinner_path.removeClass('is-active');
                $spinner_circular.removeClass('is-active');
                showDialog({
                    title: 'Fehler!',
                    text: 'Leider konnte Ihr Vorschlag nicht übermittelt werden. Bitte versuchen Sie es erneut.<br><br>Fehler: '
                    + error.message,
                    positive: {
                        title: 'OK',
                        onClick: function (e) {

                        }
                    }
                });
            }
        });
    }
});

$(document).on("click", "#loadmore", function () {

    var $container = $('#containerisotope');

    skip = skip + 25;

    // delete loadmore button
    $('#loadmore').parent().remove();
    $container.isotope('layout');

    // load another 20
    downloadQuotes();
});

$(document).on("click", ".sharelayout img", function () {

    var quote = $(this).closest('.element').find('.blockquotetile').text();
    var source = $(this).closest('.element').find('.small').text();
    var shareText = quote + " - " + source;

    if ($(this).attr("class").indexOf("fb") > -1) {
        FB.ui({
            method: 'feed',
            name: 'easeWave.com',
            caption: 'freshen up your brain',
            description: (shareText),
            link: 'http://easewave.com',
            redirect_uri: 'http://easewave.com',
            picture: 'http://www.easewave.com/img/newlogo2015.png'
        }, function (response) {
            if (response && response.post_id) {
                //alert('Post was published.');
            } else {
                //alert('Post was not published.');
            }
        });
    } else if ($(this).attr("class").indexOf("google") > -1) {
        /*var options = {
         contenturl: 'http://easewave.com',
         clientid: '1089474815594-3k0nc81m1pubnf0j19pj0slvs2tj7g2m.apps.googleusercontent.com',
         cookiepolicy: 'none',
         prefilltext: shareText,
         calltoactionlabel: 'VIEW',
         calltoactionurl: 'http://easewave.com'
         };
         gapi.interactivepost.render($(this).attr("id"), options);*/
        //$(this).click();
    } else if ($(this).attr("class").indexOf("mail") > -1) {
        var shareUrl1 = escape("http://easewave.com");
        window.location = 'mailto:?subject=freshen up your brain&body=' + shareText + '%0A%0A' + shareUrl1;
    } else if ($(this).attr("class").indexOf("twitter") > -1) {
        shareText = "\"" + quote + "\" -" + source.replace("DIE BIBEL", "");
        var shareUrl = "#easeWave";
        var maxLength = 140 - (shareUrl.length + 1);
        if (shareText.length > maxLength) {
            var difference = shareText.length - maxLength;
            quote = quote.substring(0, quote.length - difference - 3) + "...";
            shareText = "\"" + quote + "\" -" + source.replace("DIE BIBEL", "");
        }
        var twtLink = 'http://twitter.com/home?status=' + encodeURIComponent(shareText + ' ' + shareUrl);
        window.open(twtLink);
    } else if ($(this).attr("class").indexOf("fav") > -1) {
        if (localStorage.getItem('fav' + quote) === null) {
            var existingFavs = false;
            for (var i = 0, len = localStorage.length; i < len; ++i) {
                if (localStorage.key(i).indexOf('fav') > -1) {
                    existingFavs = true;
                }
            }
            if (!existingFavs) {
                showDialog({
                    title: 'Hinweis',
                    text: 'Favoriten werden lokal im Cache Ihres Browsers gespeichert. ' +
                    'Durch das löschen von Browserdaten bzw. Caches können Ihre gespeicherten Favoriten verloren gehen!',
                    positive: {
                        title: 'OK',
                        onClick: function (e) {

                        }
                    }
                });
            }
            saveFav(quote, source);
            $(this).attr('src', '../img/star7.png');
            $(this).attr('title', 'von Favoriten entfernen');
            $(this).closest('.element').removeClass('white').addClass('color');
        } else {
            deleteFav(quote);
            $(this).attr('src', '../img/stars59.png');
            $(this).attr('title', 'zu Favoriten hinzufügen');
            $(this).closest('.element').removeClass('color').addClass('white');
        }
        if ($title.text().indexOf('Fav') > -1) {
            $(this).closest('.element').remove();
            if ($container.children().length > 0) {
                $container.isotope('remove', $(this).closest('.element')).isotope('layout');
            } else {
                // no favs
                $container.isotope('remove', $container.isotope('getItemElements'));
                $alpha.append(nofavscard);
                $container.isotope({
                    filter: '*',
                    animationOptions: {
                        duration: 750,
                        easing: 'linear',
                        queue: false
                    }
                }).isotope('insert', $alpha.find('.element'));
                $container.isotope('layout');
                $searchbar.transition({opacity: 0, duration: 700, easing: 'cubic-bezier(.53,.01,.83,.75)'});
                $searchbar.hide();
            }
            downloadedFavs = $container.html();
        }
    }
});

$(document).on({
    mouseenter: function (event) {
        var small = $(this).find(".small");
        var sharelayout = $(this).find(".sharelayout");
        small.transition({y: '40px', duration: 200, easing: 'easeInOutQuad'});
        setTimeout(function () {
            small.hide();
            sharelayout.show();
            sharelayout.transition({y: '0px', duration: 200, easing: 'easeInOutQuad'});
        }, 250);
        event.preventDefault();
    },
    mouseleave: function () {
        var small = $(this).find(".small");
        var sharelayout = $(this).find(".sharelayout");
        sharelayout.transition({y: '40px', duration: 200, easing: 'easeInOutQuad'});
        setTimeout(function () {
            sharelayout.hide();
            small.show();
            small.transition({y: '0px', duration: 200, easing: 'easeInOutQuad'});
        }, 250);
        event.preventDefault();
    }
}, '.element.home');

$(document).on("click", ".element.home", function (event) {
    var small = $(this).find(".small");
    var sharelayout = $(this).find(".sharelayout");
    if ($(this).find(".sharelayout").is(":visible")) {
        sharelayout.transition({y: '40px', duration: 200, easing: 'easeInOutQuad'});
        setTimeout(function () {
            sharelayout.hide();
            small.show();
            small.transition({y: '0px', duration: 200, easing: 'easeInOutQuad'});
        }, 250);
        event.preventDefault();
    }
});

$search_expandable.blur(function () {
    if ($search_expandable.val() === "") {
        setTimeout(function () {
            $search_close.hide();
            $('#title').removeClass('hidethis');
        }, 100);
    }
});

$search_expandable.focus(function () {
    setTimeout(function () {
        $search_close.show();
        $('#title').addClass('hidethis');
    }, 101);
});

$('#search-icon').click(function (event) {
    if ($search_expandable.val() !== "") {
        var array1 = $.trim($search_expandable.val()).split(' ');
        $search_expandable.val('');
        for (var j = 0; j < array1.length; j++) {
            $search_expandable.val($search_expandable.val() + '#' + array1[j].replace('#', '').replace(',', '') + ', ');
        }
        if ($title.text().indexOf('Fav') > -1) {
            searchFavs();
        } else if ($title.text().indexOf('durchsuchen') > -1) {
            searchQuotes();
        }
    } else {
        if (!$('#title').hasClass('hidethis')) {
            $('#title').addClass('hidethis');
        }
    }
    $search_close.show();
});

$searchbar.click(function () {
    if ($search_expandable.val() === "") {
        if (!$('#title').hasClass('hidethis')) {
            $('#title').addClass('hidethis');
        }
        $search_close.show();
    }
});

$search_close.click(function () {
    if ($search_expandable.val() !== "") {
        if ($title.text().indexOf('Fav') > -1) {
            reloadDownloadedFavs();
        } else if ($title.text().indexOf('durchsuchen') > -1) {
            reloadDownloadedQuotes();
        }
    }
    $search_expandable.val("").blur();
    $searchbar.removeClass('is-dirty');
    setTimeout(function () {
        $search_close.hide();
        $('#title').removeClass('hidethis');
    }, 100);
});

$search_expandable.keyup(function (e) {
    if (e.keyCode == 13) { // enter
        if ($search_expandable.val() !== "") {
            var array1 = $.trim($(this).val()).split(' ');
            $(this).val('');
            for (var j = 0; j < array1.length; j++) {
                $search_expandable.val($search_expandable.val() + '#' + array1[j].replace('#', '').replace(',', '') + ', ');
            }
            if ($title.text().indexOf('Fav') > -1) {
                searchFavs();
            } else if ($title.text().indexOf('durchsuchen') > -1) {
                searchQuotes();
            }
        }
    }
    if (e.keyCode == 27) { // escape
        if ($search_expandable.val() !== "") {
            if ($title.text().indexOf('Fav') > -1) {
                reloadDownloadedFavs();
            } else if ($title.text().indexOf('durchsuchen') > -1) {
                reloadDownloadedQuotes();
            }
        }
        $search_expandable.val("").blur();
        $searchbar.removeClass('is-dirty');
        setTimeout(function () {
            $search_close.hide();
            $('#title').removeClass('hidethis');
        }, 100);
    }
    if (e.keyCode == 32) { // space
        var array = $.trim($(this).val()).split(' ');
        $(this).val('');
        for (var i = 0; i < array.length; i++) {
            $search_expandable.val($search_expandable.val() + '#' + array[i].replace('#', '').replace(',', '') + ', ');
        }
    }
});

$(document).on("submit", "#contactform", function () {

    var action = $(this).attr('action');

    $.post(action, {
            name: $('#name').val(),
            email: $('#email').val(),
            comments: $('#comments').val()
        },
        function (data) {
            var title = 'Herzlichen Dank!';
            if (data.indexOf('error_message') > -1) {
                title = 'Fehler!';
            } else {
                document.getElementById('name').value = '';
                document.getElementById('email').value = '';
                document.getElementById('comments').value = '';
                $('#name').parent().removeClass('is-dirty');
                $('#email').parent().removeClass('is-dirty');
                setTimeout(function () {
                    if ($('#comments').val() != '') {
                        $('#contact_send').attr('disabled', false);
                    } else {
                        $('#contact_send').attr('disabled', true);
                    }
                }, 200);
            }
            showDialog({
                title: title,
                text: data,
                positive: {
                    title: 'OK',
                    onClick: function (e) {

                    }
                }
            });
        });
    return false;
});

function getFormattedPartTime(partTime) {
    if (partTime < 10)
        return "0" + partTime;
    return partTime;
}

function drawerHandler(position) {
    $('.darker').trigger("click");
    hideCurrentContent(currentDrawerPositon);
    currentDrawerPositon = position;
    $(".nav__list li").css("background-color", "#FFF");
    $(".nav__list li:nth-child(" + position + ")").css("background-color", "#d1d1d1");
    setTimeout(function () {
    }, 1900);
    switch (position) {
        case 1:
            $title.text("Heutiges Zitat");
            setTimeout(function () {
                todaysquote();
            }, 1100);
            break;
        case 2:
            $title.text("Favoriten");
            setTimeout(function () {
                favorites();
            }, 1100);
            break;
        case 3:
            $title.text("Zitate durchsuchen");
            drawerenabled = false;
            setTimeout(function () {
                browse();
            }, 1100);
            break;
        case 4:
            $title.text("Zitat voschlagen");
            setTimeout(function () {
                submitquote();
            }, 1100);
            break;
        case 7:
            $title.text("Über");
            setTimeout(function () {
                about();
            }, 1100);
            break;
        case 8:
            $title.text("Kontakt");
            setTimeout(function () {
                contact();
            }, 1100);
            break;
        case 11:
            $title.text("Android App");
            setTimeout(function () {
                androidapp();
            }, 1100);
            break;
        case 12:
            $title.text("Chrome Extension");
            setTimeout(function () {
                chromeExtension();
            }, 1100);
            break;
        case 13:
            $title.text("Stimmungstagebuch");
            setTimeout(function () {
                moodDiary();
            }, 1100);
            break;
    }
}

function hideCurrentContent(position) {

    switch (position) {
        case 1:
            //$myResults.velocity({opacity: 0}, 1000, "swing");
            $myResults.transition({opacity: 0, duration: 700, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            //$myResultssource.velocity({opacity: 0}, 1000, "swing");
            $myResultssource.transition({opacity: 0, duration: 700, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            //$fabShare.velocity({scale: 0}, 300, [.46, .72, .71, 1.42]);
            $fabShare.transition({scale: 0, duration: 300, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            setTimeout(function () {
                $fabShare.hide();
                $(".sharemenu").hide();
            }, 400);
            setTimeout(function () {
                $myResults.hide();
                $myResultssource.hide();
            }, 1100);
            break;
        case 2:
            var $container = $('#containerisotope');
            $container.isotope('remove', $container.isotope('getItemElements'));
            $container.css('height', 0);
            $search_expandable.val("").blur();
            $searchbar.removeClass('is-dirty');
            setTimeout(function () {
                $search_close.hide();
            }, 100);
            $searchbar.transition({opacity: 0, duration: 500, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            $searchbar.hide();
            break;
        case 3:
            var $container = $('#containerisotope');
            $container.isotope('remove', $container.isotope('getItemElements'));
            $container.css('height', 0);
            $search_expandable.val("").blur();
            $searchbar.removeClass('is-dirty');
            setTimeout(function () {
                $search_close.hide();
            }, 100);
            $searchbar.transition({opacity: 0, duration: 500, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            $searchbar.hide();
            break;
        case 4:
            $submit.velocity("slideUp", {duration: 500});
            //$submit_send.velocity({scale: 0}, 300, [.46, .72, .71, 1.42]);
            $submit_send.transition({scale: 0, duration: 300, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            setTimeout(function () {
                $submit_send.hide();
            }, 400);
            setTimeout(function () {
                $submit.hide();
            }, 1000);
            break;
        case 7:
            //$aboutlayout.velocity({opacity: 0}, 1000, "swing");
            $aboutlayout.velocity("slideUp", {duration: 500});
            setTimeout(function () {
                $aboutlayout.hide();
            }, 1000);
            break;
        case 8:
            //$contactlayout.velocity({opacity: 0}, 1000, "swing");
            $contactlayout.velocity("slideUp", {duration: 500});
            //$contact_send.velocity({scale: 0}, 300, [.46, .72, .71, 1.42]);
            $contact_send.transition({scale: 0, duration: 300, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            setTimeout(function () {
                $contact_send.hide();
            }, 400);
            setTimeout(function () {
                $contactlayout.hide();
            }, 1000);
            break;
        case 11:
            $androidapplayout.velocity("slideUp", {duration: 500});
            setTimeout(function () {
                $androidapplayout.hide();
            }, 1000);
            break;
        case 12:
            $chromeextensionlayout.velocity("slideUp", {duration: 500});
            setTimeout(function () {
                $chromeextensionlayout.hide();
            }, 1000);
            break;
        case 13:
            $mooddiarylayout.velocity("slideUp", {duration: 500});
            setTimeout(function () {
                $mooddiarylayout.hide();
            }, 1000);
            break;
    }
}

function todaysquote() {
    // Start Simulated Quote ***********************************//
    /*var quote = 'Sei mutig und entschlossen! Lass dich nicht einschüchtern, und hab keine Angst! Denn ich, der Herr, dein Gott, bin bei dir, wohin du auch gehst.';
     var source = "DIE BIBEL Josua 1,9";
     document.getElementById("myResults").innerHTML = quote;
     document.getElementById("myResultssource").innerHTML = source;
     if (localStorage.getItem('fav' + quote) !== null) {
     $fab_fav_img.attr('src', '../img/fav_full_square.png');
     $fab_fav_img.attr('title', 'von Favoriten entfernen');
     $('#favorite').attr('title', 'von Favoriten entfernen');
     } else {
     $fab_fav_img.attr('src', '../img/fav_empty_square.png');
     $fab_fav_img.attr('title', 'zu Favoriten hinzufügen');
     $('#favorite').attr('title', 'zu Favoriten hinzufügen');
     }

     $mdl_spinner.hide();
     $spinner_path.removeClass('is-active');
     $spinner_circular.removeClass('is-active');
     $myResults.show();
     $myResultssource.show();
     //$myResults.velocity({opacity: 1}, 1000, "swing");
     $myResults.transition({opacity: 1, duration: 1000, easing: 'cubic-bezier(.53,.01,.83,.75)'});
     //$myResultssource.velocity({opacity: 1}, 1000, "swing");
     $myResultssource.transition({opacity: 1, duration: 1000, easing: 'cubic-bezier(.53,.01,.83,.75)'});
     $fabShare.transition({rotate: '360deg', duration: 0});
     setTimeout(function () {
     $fabShare.show();
     $(".sharemenu").show();
     //$fabShare.velocity({scale: 1}, 300, [.46, .72, .71, 1.42]);
     $fabShare.transition({scale: 1, duration: 300, easing: 'cubic-bezier(0.46, 0.72, 0.71, 1.42)'});
     }, 1100);*/
    // End Simulated Quote ***********************************//

    if (!$spinner_circular.hasClass('is-active')) {
        $spinner_path.addClass('is-active');
        $spinner_circular.addClass('is-active');
        $mdl_spinner.show();
    }

    query.equalTo("Date", today);
    query.find({
        success: function (results) {
            if (results.length > 0) {
                for (var i = 0; i < results.length; i++) {
                    var object = results[i];
                    var quote = object.get('de');
                    var source = object.get('vers').replace('<b>', '').replace('</b>', '');
                    if (localStorage.getItem('fav' + quote) !== null) {
                        $fab_fav_img.attr('src', '../img/fav_full_square.png');
                        $fab_fav_img.attr('title', 'von Favoriten entfernen');
                        $('#favorite').attr('title', 'von Favoriten entfernen');
                    } else {
                        $fab_fav_img.attr('src', '../img/fav_empty_square.png');
                        $fab_fav_img.attr('title', 'zu Favoriten hinzufügen');
                        $('#favorite').attr('title', 'zu Favoriten hinzufügen');
                    }
                    if (quote) {
                        $mdl_spinner.hide();
                        $spinner_path.removeClass('is-active');
                        $spinner_circular.removeClass('is-active');
                        document.getElementById("myResults").innerHTML = quote;
                        document.getElementById("myResultssource").innerHTML = source;
                        $myResults.show();
                        $myResultssource.show();
                        $myResults.transition({
                            opacity: 1,
                            duration: 1000,
                            easing: 'cubic-bezier(.53,.01,.83,.75)'
                        });
                        $myResultssource.transition({
                            opacity: 1,
                            duration: 1000,
                            easing: 'cubic-bezier(.53,.01,.83,.75)'
                        });
                        $fabShare.transition({rotate: '360deg', duration: 0});
                        setTimeout(function () {
                            $fabShare.show();
                            $(".sharemenu").show();
                            $fabShare.transition({
                                scale: 1,
                                duration: 300,
                                easing: 'cubic-bezier(0.46, 0.72, 0.71, 1.42)'
                            });
                        }, 1200);
                        gRenderShare(quote, source, 'googleplus');
                    } else {
                        loadYesterdaysQuote();
                    }
                }
            } else {
                loadYesterdaysQuote();
            }
        },
        error: function () {
            loadYesterdaysQuote();
        }
    });
}

function about() {
    $aboutlayout.show();
    $aboutlayout.velocity("slideDown", {duration: 500});

    // Text Rotator
    $('.rotate').each(function () {
        var el = $(this);
        var text = $(this).html().split(",");
        el.html(text[0]);
        setInterval(function () {
            el.animate({
                textShadowBlur: 20,
                opacity: 0
            }, 500, function () {
                var index = $.inArray(el.html(), text);
                if ((index + 1) == text.length) index = -1;
                el.text(text[index + 1]).animate({
                    textShadowBlur: 0,
                    opacity: 1
                }, 500);
            });
        }, 2000);
    });
}

function browse() {
    $browselayout.show();
    $browselayout.transition({opacity: 1, duration: 0});

    skip = 0;

    downloadQuotes();

    $spinner_path.addClass('is-active');
    $spinner_circular.addClass('is-active');
    $mdl_spinner.show();

    // load new quotes automatically (scroll listener)
    /*$('#content').on('scroll', function () {
     if (!loadingmore) {
     if ($(this).scrollTop() + ($(this).innerHeight()*0.6) + $(this).innerHeight() >= $(this)[0].scrollHeight) {
     loadingmore = true;
     console.log("load more");
     $('.card-spinner').addClass('is-active');
     skip = skip + 25;
     downloadQuotes();
     }
     }
     });*/
}

function favorites() {
    $browselayout.show();
    $browselayout.transition({opacity: 1, duration: 0});

    readFavs();
}

function saveFav(quote, source) {
    //localStorage.clear();
    var fav = [quote, source];
    localStorage['fav' + quote] = JSON.stringify(fav);
}

function deleteFav(quote) {
    localStorage.removeItem('fav' + quote);
}

function readFavs() {

    drawerenabled = false;
    $spinner_path.addClass('is-active');
    $spinner_circular.addClass('is-active');
    $mdl_spinner.show();
    var existingFavs = false;

    for (var i = 0, len = localStorage.length; i < len; ++i) {
        if (localStorage.key(i).indexOf('fav') > -1) {
            existingFavs = true;
            var fav = JSON.parse(localStorage.getItem(localStorage.key(i)));

            var card = cardtemplate.replace('/quote/', fav[0]).replace('/source/', fav[1]);
            card = card.replace('src="../img/stars59.png"', 'src="../img/star7.png"')
                .replace('title="zu Favoriten hinzufügen"', 'title="von Favoriten entfernen"')
                .replace('white', 'color').replace('/id/', 'googleplus' + i);
            $alpha.append(card);
            gRenderShare(fav[0], fav[1], 'googleplus' + i);
            $('.sharelayout').transition({y: '40px', duration: 0});
            $('.sharelayout').hide();
        }
    }
    if (!existingFavs) {
        $alpha.append(nofavscard);
        $searchbar.transition({opacity: 0, duration: 700, easing: 'cubic-bezier(.53,.01,.83,.75)'});
        $searchbar.hide();
    } else {
        $searchbar.css('display', 'inline-block');
        $searchbar.show();
        $searchbar.transition({opacity: 1, duration: 700, easing: 'cubic-bezier(.53,.01,.83,.75)'});
    }

    downloadedFavs = $alpha.html();
    $container.isotope({
        filter: '*',
        animationOptions: {
            duration: 750,
            easing: 'linear',
            queue: false
        },
        masonry: {
            isFitWidth: true
        }
    }).isotope('insert', $alpha.find('.element'));
    $container.isotope('layout');
    $mdl_spinner.hide();
    $spinner_path.removeClass('is-active');
    $spinner_circular.removeClass('is-active');
    drawerenabled = true;

}

function searchFavs() {

    drawerenabled = false;
    $spinner_path.addClass('is-active');
    $spinner_circular.addClass('is-active');
    $mdl_spinner.show();
    $container.isotope('remove', $container.isotope('getItemElements'));
    var searchtext = $.trim($search_expandable.val()).slice(0, -1);
    var searchTerms = searchtext.toLowerCase().split(',');
    var foundFavs = false;
    for (var j = 0; j < searchTerms.length; j++) {
        var currentterm = searchTerms[j].replace('#', '');
        for (var i = 0, len = localStorage.length; i < len; ++i) {
            if (localStorage.key(i).indexOf('fav') > -1) {
                var fav = JSON.parse(localStorage.getItem(localStorage.key(i)));
                if (fav[0].toLowerCase().indexOf(currentterm) > -1 || fav[1].toLowerCase().indexOf(currentterm) > -1) {
                    if ($alpha.find('.element .blockquotetile:contains("' + fav[0] + '")').length > 0) {
                        //console.log("duplicate");
                    } else {
                        //console.log("new");
                        foundFavs = true;
                        var card = cardtemplate.replace('/quote/', fav[0]).replace('/source/', fav[1]);
                        card = card.replace('src="../img/stars59.png"', 'src="../img/star7.png"')
                            .replace('title="zu Favoriten hinzufügen"', 'title="von Favoriten entfernen"')
                            .replace('white', 'color').replace('/id/', 'googleplus' + i);
                        ;
                        $alpha.append(card);
                        gRenderShare(fav[0], fav[1], 'googleplus' + i);
                        $('.sharelayout').transition({y: '40px', duration: 0});
                        $('.sharelayout').hide();
                    }
                }
            }
        }
    }
    if (!foundFavs) {
        // no search results
        $alpha.append(nosearchresultscard);
    }
    $container.isotope({
        filter: '*',
        animationOptions: {
            duration: 750,
            easing: 'linear',
            queue: false
        },
        masonry: {
            isFitWidth: true
        }
    }).isotope('insert', $alpha.find('.element'));
    $container.isotope('layout');
    $mdl_spinner.hide();
    $spinner_path.removeClass('is-active');
    $spinner_circular.removeClass('is-active');
    drawerenabled = true;
    for (var k = 0; k < searchTerms.length; k++) {
        var currentterm2 = searchTerms[k].replace('#', '');
        $('.blockquotetile').highlight(currentterm2, "highlight");
        $('.small').highlight(currentterm2, "highlight");
    }
}

function searchQuotes() {

    drawerenabled = false;
    $spinner_path.addClass('is-active');
    $spinner_circular.addClass('is-active');
    $mdl_spinner.show();
    var ParseObj = Parse.Object.extend("Quotes");
    var arrayOfQueries = [];
    $container.isotope('remove', $container.isotope('getItemElements'));
    var searchtext = $.trim($search_expandable.val()).slice(0, -1);
    var searchTerms = searchtext.toLowerCase().split(',');
    var foundFavs = false;
    for (var i = 0; i < searchTerms.length; i++) {
        var currentterm = searchTerms[i].replace('#', '');
        var quotequery = new Parse.Query(ParseObj);
        quotequery.contains("de_search", currentterm);
        var sourcequery = new Parse.Query(ParseObj);
        sourcequery.contains("vers_search", currentterm);
        arrayOfQueries.push(quotequery);
        arrayOfQueries.push(sourcequery);
    }
    var mainQuery = Parse.Query.or.apply(this, arrayOfQueries);
    mainQuery.find({
        success: function (results) {
            foundFavs = true;
            for (var i = 0; i < results.length; i++) {
                if ($alpha.find('.element .blockquotetile:contains("' + results[i].get("de") + '")').length > 0) {
                } else {
                    var card = cardtemplate.replace('/quote/', results[i].get("de"))
                        .replace('/source/', results[i].get("vers").replace("<b>", "").replace("</b>", ""))
                        .replace('/id/', 'googleplus' + i);
                    ;
                    if (localStorage.getItem('fav' + results[i].get("de")) !== null) {
                        card = card.replace('src="../img/stars59.png"', 'src="../img/star7.png"')
                            .replace('title="zu Favoriten hinzufügen"', 'title="von Favoriten entfernen"')
                            .replace('white', 'color');
                    }
                    $alpha.append(card);
                    gRenderShare(results[i].get("de"), results[i].get("vers").replace("<b>", "").replace("</b>", ""), 'googleplus' + i);
                    $('.sharelayout').transition({y: '40px', duration: 0});
                    $('.sharelayout').hide();
                }
            }
            $container.isotope({
                filter: '*',
                animationOptions: {
                    duration: 750,
                    easing: 'linear',
                    queue: false
                },
                masonry: {
                    isFitWidth: true
                }
            }).isotope('insert', $alpha.find('.element'));
            $container.isotope('layout');
            $searchbar.css('display', 'inline-block');
            $searchbar.show();
            $searchbar.transition({opacity: 1, duration: 700, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            $mdl_spinner.hide();
            $spinner_path.removeClass('is-active');
            $spinner_circular.removeClass('is-active');
            drawerenabled = true;
            for (var j = 0; j < searchTerms.length; j++) {
                var currentterm2 = searchTerms[j].replace('#', '');
                $('.blockquotetile').highlight(currentterm2, "highlight");
                $('.small').highlight(currentterm2, "highlight");
            }
        },
        error: function (error) {
            // There was an error.
            showDialog({
                title: 'Fehler!',
                text: error,
                positive: {
                    title: 'OK',
                    onClick: function (e) {

                    }
                }
            });
            $mdl_spinner.hide();
            $spinner_path.removeClass('is-active');
            $spinner_circular.removeClass('is-active');
            drawerenabled = true;
        }
    });
}

function downloadQuotes() {

    var ParseObj = Parse.Object.extend("Quotes");
    var query = new Parse.Query(ParseObj);
    query.limit(25);
    query.skip(skip);
    query.descending("Date");
    query.lessThanOrEqualTo("Date", today);
    query.find({
        success: function (results) {
            for (var i = 0; i < results.length; i++) {
                var card = cardtemplate.replace('/quote/', results[i].get("de"))
                    .replace('/source/', results[i].get("vers").replace("<b>", "").replace("</b>", ""))
                    .replace('/id/', 'googleplus' + i);
                if (localStorage.getItem('fav' + results[i].get("de")) !== null) {
                    card = card.replace('src="../img/stars59.png"', 'src="../img/star7.png"')
                        .replace('title="zu Favoriten hinzufügen"', 'title="von Favoriten entfernen"')
                        .replace('white', 'color');
                }
                $alpha.append(card);
                gRenderShare(results[i].get("de"), results[i].get("vers").replace("<b>", "").replace("</b>", ""), 'googleplus' + i);
                $('.sharelayout').transition({y: '40px', duration: 0});
                $('.sharelayout').hide();
            }
            //$alpha.text().replace('loadingmorecard', '');
            $alpha.append(loadmorecard);
            //$alpha.append(loadingmorecard);
            downloadedQuotes = $alpha.html();
            $container.isotope({
                filter: '*',
                animationOptions: {
                    duration: 750,
                    easing: 'linear',
                    queue: false
                },
                masonry: {
                    isFitWidth: true
                }
            }).isotope('insert', $alpha.find('.element'));
            $container.isotope('layout');
            $searchbar.css('display', 'inline-block');
            $searchbar.show();
            $searchbar.transition({opacity: 1, duration: 700, easing: 'cubic-bezier(.53,.01,.83,.75)'});
            loadingmore = false;
            $mdl_spinner.hide();
            $spinner_path.removeClass('is-active');
            $spinner_circular.removeClass('is-active');
            drawerenabled = true;
        }
    });
}

function reloadDownloadedQuotes() {

    $spinner_path.addClass('is-active');
    $spinner_circular.addClass('is-active');
    $mdl_spinner.show();
    $container.isotope('remove', $container.isotope('getItemElements'));
    $container.html("");
    $alpha.html(downloadedQuotes);
    $container.isotope({
        filter: '*',
        animationOptions: {
            duration: 750,
            easing: 'linear',
            queue: false
        },
        masonry: {
            isFitWidth: true
        }
    }).isotope('insert', $alpha.find('.element'));
    $container.isotope('layout');
    $searchbar.css('display', 'inline-block');
    $searchbar.show();
    $searchbar.transition({opacity: 1, duration: 700, easing: 'cubic-bezier(.53,.01,.83,.75)'});
    // render g+ buttons on all cards
    $container.find('.element').each(function (i) {
        var quote = $(this).find('.blockquotetile').text();
        var source = $(this).find('.small').text();
        gRenderShare(quote, source, $(this).find('.google').attr('id'));
    });
    $mdl_spinner.hide();
    $spinner_path.removeClass('is-active');
    $spinner_circular.removeClass('is-active');
}

function reloadDownloadedFavs() {

    $spinner_path.addClass('is-active');
    $spinner_circular.addClass('is-active');
    $mdl_spinner.show();
    $container.isotope('remove', $container.isotope('getItemElements'));
    $container.html("");
    $alpha.html(downloadedFavs);
    $container.isotope({
        filter: '*',
        animationOptions: {
            duration: 750,
            easing: 'linear',
            queue: false
        },
        masonry: {
            isFitWidth: true
        }
    }).isotope('insert', $alpha.find('.element'));
    $container.isotope('layout');
    $searchbar.css('display', 'inline-block');
    $searchbar.show();
    $searchbar.transition({opacity: 1, duration: 700, easing: 'cubic-bezier(.53,.01,.83,.75)'});
    // render g+ buttons on all cards
    $container.find('.element').each(function (i) {
        var quote = $(this).find('.blockquotetile').text();
        var source = $(this).find('.small').text();
        gRenderShare(quote, source, $(this).find('.google').attr('id'));
    });
    $mdl_spinner.hide();
    $spinner_path.removeClass('is-active');
    $spinner_circular.removeClass('is-active');
}

function submitquote() {
    $submit.show();
    $submit.velocity("slideDown", {duration: 500});
    setTimeout(function () {
        $submit_send.show();
        $submit_send.transition({scale: 1, duration: 300, easing: 'cubic-bezier(0.46, 0.72, 0.71, 1.42)'});
    }, 1100);

    $('#submit_send').attr('disabled', true);
    $('#submit-form').find('textarea').on('keyup', function () {
        var textarea_value = $("#quote").val();
        if (textarea_value != '') {
            $('#submit_send').attr('disabled', false);
            document.getElementById('submitformstatus').innerHTML = '';
        } else {
            $('#submit_send').attr('disabled', true);
        }
    });
}

function contact() {
    $contactlayout.show();
    $contactlayout.velocity("slideDown", {duration: 500});
    setTimeout(function () {
        $contact_send.show();
        //$contact_send.velocity({scale: 1}, 300, [.46, .72, .71, 1.42]);
        $contact_send.transition({scale: 1, duration: 300, easing: 'cubic-bezier(0.46, 0.72, 0.71, 1.42)'});
    }, 1100);

    $('#contact_send').attr('disabled', true);
    $('#contactform').find('textarea').on('keyup', function () {
        var textarea_value = $("#comments").val();
        if (textarea_value != '') {
            $('#contact_send').attr('disabled', false);
        } else {
            $('#contact_send').attr('disabled', true);
        }
    });
}

function androidapp() {
    $androidapplayout.show();
    $androidapplayout.velocity("slideDown", {duration: 500});
}

function chromeExtension() {
    $chromeextensionlayout.show();
    $chromeextensionlayout.velocity("slideDown", {duration: 500});
}

function moodDiary() {
    $mooddiarylayout.show();
    $mooddiarylayout.velocity("slideDown", {duration: 500});
}

function loadYesterdaysQuote() {
    var date = new Date();
    date.setDate(date.getDate() - 1);
    var month2 = getFormattedPartTime(date.getMonth() + 1);
    var day2 = getFormattedPartTime(date.getDate());
    var year2 = date.getFullYear();
    var yesterday = year2 + "/" + month2 + "/" + day2;
    query.equalTo("Date", yesterday);
    query.find({
        success: function (results) {
            for (var i = 0; i < results.length; i++) {
                var object = results[i];
                var quote = object.get('de');
                var source = object.get('vers').replace('<b>', '').replace('</b>', '');
                if (localStorage.getItem('fav' + quote) !== null) {
                    $fab_fav_img.attr('src', '../img/fav_full_square.png');
                    $fab_fav_img.attr('title', 'von Favoriten entfernen');
                    $('#favorite').attr('title', 'von Favoriten entfernen');
                } else {
                    $fab_fav_img.attr('src', '../img/fav_empty_square.png');
                    $fab_fav_img.attr('title', 'zu Favoriten hinzufügen');
                    $('#favorite').attr('title', 'zu Favoriten hinzufügen');
                }
                if (quote) {
                    $mdl_spinner.hide();
                    $spinner_path.removeClass('is-active');
                    $spinner_circular.removeClass('is-active');
                    document.getElementById("myResults").innerHTML = quote;
                    document.getElementById("myResultssource").innerHTML = source;
                    $myResults.show();
                    $myResultssource.show();
                    $myResults.transition({
                        opacity: 1,
                        duration: 1000,
                        easing: 'cubic-bezier(.53,.01,.83,.75)'
                    });
                    $myResultssource.transition({
                        opacity: 1,
                        duration: 1000,
                        easing: 'cubic-bezier(.53,.01,.83,.75)'
                    });
                    $fabShare.transition({rotate: '360deg', duration: 0});
                    setTimeout(function () {
                        $fabShare.show();
                        $(".sharemenu").show();
                        $fabShare.transition({
                            scale: 1,
                            duration: 300,
                            easing: 'cubic-bezier(0.46, 0.72, 0.71, 1.42)'
                        });
                    }, 1200);
                    gRenderShare(quote, source, 'googleplus');
                } else {
                    showDialog({
                        title: 'Fehler!',
                        text: 'Das Zitat des Tages konnte leider nicht geladen werden. Bitte versuchen Sie es noch einmal!',
                        positive: {
                            title: 'OK',
                            onClick: function (e) {

                            }
                        }
                    });
                }
            }
        },
        error: function (error) {
        }
    });
}

function hidesharebar() {
    $fabShare.trigger("click");
}

function fabFavorite_click() {
    var quote = document.getElementById('myResults').innerHTML;
    var source = document.getElementById('myResultssource').innerHTML;
    if (localStorage.getItem('fav' + quote) === null) {
        // hinweis über speicherung (löschen)
        var existingFavs = false;
        for (var i = 0, len = localStorage.length; i < len; ++i) {
            if (localStorage.key(i).indexOf('fav') > -1) {
                existingFavs = true;
            }
        }
        if (!existingFavs) {
            showDialog({
                title: 'Hinweis',
                text: 'Favoriten werden lokal im Cache Ihres Browsers gespeichert. ' +
                'Durch das löschen von Browserdaten bzw. Caches können Ihre gespeicherten Favoriten verloren gehen!',
                positive: {
                    title: 'OK',
                    onClick: function (e) {

                    }
                }
            });
        }
        saveFav(quote, source);
        $fab_fav_img.attr('src', '../img/fav_full_square.png');
        $fab_fav_img.attr('title', 'von Favoriten entfernen');
        $('#favorite').attr('title', 'von Favoriten entfernen');
    } else {
        deleteFav(quote);
        $fab_fav_img.attr('src', '../img/fav_empty_square.png');
        $fab_fav_img.attr('title', 'zu Favoriten hinzufügen');
        $('#favorite').attr('title', 'zu Favoriten hinzufügen');
    }
    setTimeout(function () {
        $fabShare.trigger("click");
    }, 200);
}

function email_click() {
    $fabShare.trigger("click");
    var str1 = document.getElementById('myResults').innerHTML;
    var str2 = document.getElementById('myResultssource').innerHTML;
    var twtTitle = str1.replace('"', '').replace('"', '') + '  - ' + str2.replace("<b>", "").replace("</b>", "").replace('"', '');
    var twtUrl = "http://easewave.com";
    window.location = 'mailto:?subject=freshen up your brain&body=' + twtTitle + '%0A%0A' + twtUrl;
}

function twitter_click() {
    $fabShare.trigger("click");
    var str1 = document.getElementById('myResults').innerHTML;
    var str2 = document.getElementById('myResultssource').innerHTML;
    var twtTitle = "\"" + str1 + "\" -" + str2.replace("DIE BIBEL", "");
    var twtUrl = "#easeWave";
    var maxLength = 140 - (twtUrl.length + 1);
    if (twtTitle.length > maxLength) {
        var difference = twtTitle.length - maxLength;
        str1 = str1.substr(0, str1.length - difference - 3) + '...';
        twtTitle = "\"" + str1 + "\" -" + str2.replace("DIE BIBEL", "");
    }
    var twtLink = 'http://twitter.com/home?status=' + encodeURIComponent(twtTitle + ' ' + twtUrl);
    window.open(twtLink);
}

function fb_click() {
    hidesharebar();
    var str1 = document.getElementById('myResults').innerHTML;
    var str2 = document.getElementById('myResultssource').innerHTML;
    var twtTitle = str1.replace('"', '').replace('"', '') + ' - ' + str2.replace("<b>", "").replace("</b>", "").replace('"', '');

    FB.ui({
        method: 'feed',
        name: 'easeWave.com',
        caption: 'freshen up your brain',
        description: (twtTitle),
        link: 'http://easewave.com',
        redirect_uri: 'http://easewave.com',
        picture: 'http://www.easewave.com/img/newlogo2015.png'
    }, function (response) {
        if (response && response.post_id) {
            //alert('Post was published.');
        } else {
            //alert('Post was not published.');
        }
    });
}

function gRenderShare(quote, source, objectid) {
    var twtTitle = quote.replace('"', '').replace('"', '') + '  - ' + source.replace("<b>", "").replace("</b>", "").replace('"', '');
    var options = {
        contenturl: 'http://easewave.com',
        clientid: '1089474815594-3k0nc81m1pubnf0j19pj0slvs2tj7g2m.apps.googleusercontent.com',
        cookiepolicy: 'none',
        prefilltext: twtTitle,
        calltoactionlabel: 'VIEW',
        calltoactionurl: 'http://easewave.com'
    };

    gapi.interactivepost.render(objectid, options);
}

function downloadApp() {
    hideAndroidAppHint();
    window.open('https://play.google.com/store/apps/details?id=com.jonathansautter.easewave', '_blank');
}

function readCookie() {
    var allcookies = document.cookie;
    //alert("All Cookies : " + allcookies);

    // Get all the cookies pairs in an array
    var cookiearray = allcookies.split(';');

    // Now take key value pair out of this array
    var value;
    var name;
    for (var i = 0; i < cookiearray.length; i++) {
        name = cookiearray[i].split('=')[0];
        value = cookiearray[i].split('=')[1];
        //alert("Key is : " + name + " and Value is : " + value);
    }
    if (value == "hideBar") {
        //alert("already showed");
    } else {
        //alert("not showed");
        setTimeout(showAndroidAppHint, 3000);
    }
}

function showAndroidAppHint() {
    //alert("show bar");
    var nav = $('.appbar');
    var ua = navigator.userAgent.toLowerCase();
    var isAndroid = ua.indexOf("android") > -1 && ua.indexOf("mobile");
    var date;
    var expires;
    if (isAndroid) {
        nav.data('size', 'small').stop().animate({
            marginTop: '150px'
        }, 300);
        // set Cookie for 30 days
        date = new Date();
        date.setTime(date.getTime() + (30 * 24 * 60 * 60 * 1000)); //30 are the days
        expires = "; expires=" + date.toGMTString();
        document.cookie = 'appcookie=hideBar' + expires + '; path=/';
    }
}

function hideAndroidAppHint() {
    var nav = $('.appbar');
    nav.data('size', 'big').stop().animate({
        marginTop: '-110px'
    }, 300);
}

window.fbAsyncInit = function () {
    FB.init({
        appId: '131721907032130',
        xfbml: true,
        status: true, // check the login status upon init?
        cookie: true, // set sessions cookies to allow your server to access the session?
        version: 'v2.2'
    });
};

(function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) {
        return;
    }
    js = d.createElement(s);
    js.id = id;
    js.src = "//connect.facebook.net/en_US/sdk.js";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));

jQuery.fn.highlight = function (str, className) {
    var regex = new RegExp(str, "gi");
    return this.each(function () {
        $(this).contents().filter(function () {
            return this.nodeType == 3 && regex.test(this.nodeValue);
        }).replaceWith(function () {
            return (this.nodeValue || "").replace(regex, function (match) {
                return "<span class=\"" + className + "\">" + match + "</span>";
            });
        });
    });
};

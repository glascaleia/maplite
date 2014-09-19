/* share42.com | 05.05.2014 | (c) Dimox 
 * 
 * Per far funzionare le script bisogna dichiarare il nume della funzione ed 
 * alla fine associare all'evento window.onload la funzione dichiarata, inoltre,
 * bisogna dichiarae u = encodeURIComponent(window.location.href);
 * Inoltre, rimuovere:
 * '"http://www.feedburner.com/fb/a/emailFlare?loc=en_US&itemTitle=' + t + '&uri=' + u + '" title="Email this to a friend"');
 * e sostituirlo con
 * '"#" onclick="window.open(\'mailto:?subject=MapLite sharing Link&body=' + u + '\', \'_blank\', \'scrollbars=0, resizable=1, menubar=0, left=100, top=100, width=550, height=400, toolbar=0, status=0\');return false" title="Send email"');
 * */
function share42() {
    var e = document.getElementsByTagName('div');
    for (var k = 0; k < e.length; k++) {
        if (e[k].className.indexOf('share42init') != -1) {
            if (e[k].getAttribute('data-url') != -1)
                var u = e[k].getAttribute('data-url');
            if (e[k].getAttribute('data-title') != -1)
                var t = e[k].getAttribute('data-title');
            if (e[k].getAttribute('data-image') != -1)
                var i = e[k].getAttribute('data-image');
            if (e[k].getAttribute('data-description') != -1)
                var d = e[k].getAttribute('data-description');
            if (e[k].getAttribute('data-path') != -1)
                var f = e[k].getAttribute('data-path');
            if (e[k].getAttribute('data-icons-file') != -1)
                var fn = e[k].getAttribute('data-icons-file');
            if (!f) {
                function path(name) {
                    var sc = document.getElementsByTagName('script'), sr = new RegExp('^(.*/|)(' + name + ')([#?]|$)');
                    for (var p = 0, scL = sc.length; p < scL; p++) {
                        var m = String(sc[p].src).match(sr);
                        if (m) {
                            if (m[1].match(/^((https?|file)\:\/{2,}|\w:[\/\\])/))
                                return m[1];
                            if (m[1].indexOf("/") == 0)
                                return m[1];
                            b = document.getElementsByTagName('base');
                            if (b[0] && b[0].href)
                                return b[0].href + m[1];
                            else
                                return document.location.pathname.match(/(.*[\/\\])/)[0] + m[1];
                        }
                    }
                    return null;
                }
                f = path('share42.js');
            }
            if (!u)
                u = location.href;
            if (!t)
                t = document.title;
            if (!fn)
                fn = 'icons.png';
            function desc() {
                var meta = document.getElementsByTagName('meta');
                for (var m = 0; m < meta.length; m++) {
                    if (meta[m].name.toLowerCase() == 'description') {
                        return meta[m].content;
                    }
                }
                return'';
            }
            if (!d)
                d = desc();
            u = encodeURIComponent(window.location.href);
            t = encodeURIComponent(t);
            t = t.replace(/\'/g, '%27');
            i = encodeURIComponent(i);
            d = encodeURIComponent(d);
            d = d.replace(/\'/g, '%27');
            var fbQuery = 'u=' + u;
            if (i != 'null' && i != '')
                fbQuery = 's=100&p[url]=' + u + '&p[title]=' + t + '&p[summary]=' + d + '&p[images][0]=' + i;
            var s = new Array('"https://www.evernote.com/clip.action?url=' + u + '&title=' + t + '" title="Share on Evernote"', '"#" data-count="fb" onclick="window.open(\'http://www.facebook.com/sharer.php?m2w&' + fbQuery + '\', \'_blank\', \'scrollbars=0, resizable=1, menubar=0, left=100, top=100, width=550, height=440, toolbar=0, status=0\');return false" title="Share on Facebook"', '"#" data-count="gplus" onclick="window.open(\'https://plus.google.com/share?url=' + u + '\', \'_blank\', \'scrollbars=0, resizable=1, menubar=0, left=100, top=100, width=550, height=440, toolbar=0, status=0\');return false" title="Share on Google+"', '"#" data-count="twi" onclick="window.open(\'https://twitter.com/intent/tweet?text=' + t + '&url=' + u + '\', \'_blank\', \'scrollbars=0, resizable=1, menubar=0, left=100, top=100, width=550, height=440, toolbar=0, status=0\');return false" title="Share on Twitter"',  '"#" onclick="window.open(\'mailto:?subject=MapLite sharing Link&body=' + u + '\', \'_blank\', \'scrollbars=0, resizable=1, menubar=0, left=100, top=100, width=550, height=400, toolbar=0, status=0\');return false" title="Send email"');
            var l = '';
            for (j = 0; j < s.length; j++)
                l += '<a rel="nofollow" style="display:inline-block;vertical-align:bottom;width:32px;height:32px;margin:0 6px 6px 0;padding:0;outline:none;background:url(' + f + fn + ') -' + 32 * j + 'px 0 no-repeat" href=' + s[j] + ' target="_blank"></a>';
            e[k].innerHTML = '<span id="share42">' + l + '</span>';
        }
    }
    ;
};

window.onload = share42();

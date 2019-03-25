$(function () {

    // init popovers
    $('[data-toggle="popover"]').popover();

    $(document).on('click', function (e) {
        $('[data-toggle="popover"],[data-original-title]')
            .each(function () {
                // the 'is' for buttons that trigger popups the 'has' for icons within a button
                // that triggers a popup
                if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
                    (($(this).popover('hide').data('bs.popover') || {}).inState || {}).click = false; // fix for BS 3.3.6
                }

            });
    });

    // prevent click through buttons
    $('.btn').click(function (evt) {
        evt.stopPropagation();
    });
    $('.btn').mouseenter(function (evt) {
        $(this)
            .parent()
            .mouseleave();
    });
    $('.btn').mouseleave(function (evt) {
        $(this)
            .parent()
            .mouseenter();
    });

    // init tooltips
    $('[data-toggle="tooltip"]').tooltip();

    // init TOC
    $("#toc").tocify({selectors: "h1,h2,h3,h4,h5"});
    $('html, body').animate({
        scrollTop: ($(window).scrollTop() + 1)
    }, 'slow'); // scroll to the top

    // line-through deprecated TOC items
    $(".tocify-item a:contains('DEPRECATED')").css("text-decoration", "line-through");
    $(".tocify-item a:contains('DEPRECATED')").each(function () {
        var htmlBefore = $(this).html();
        $(this).html(htmlBefore.replace(' - DEPRECATED', ''));
    });

    // wrap dots in toc entries to allow line breaks
    $('.tocify-item a').html(function (index, text) {
        return text.replace(/\./g, "<span class='word-wrap'></span>.").replace(/([a-z]+)([A-Z])/g, "$1<span class='word-wrap'></span>$2");
    });

    // init expandable parameters
    $(".toggleParams").click(function () {
        target = $(this).attr("href");
        $("." + target).toggleClass("in");
        $("." + target).toggleClass("out");

        $(this).tooltip('hide');

        if ($("." + target).hasClass("out")) {
            $(this).removeClass("expanded");
            $("." + target).removeClass("expanded");
            $("tr[class^='" + target + "-'],tr[class*=' " + target + "-']").removeClass("expanded");

            $("tr[class^='" + target + "-'],tr[class*=' " + target + "-']").addClass("out");
            $("tr[class^='" + target + "-'],tr[class*=' " + target + "-']").removeClass("in");

            $(this).attr('data-original-title', "Click to show contained rows");
        } else {
            $(this).addClass("expanded");

            $(this).attr('data-original-title', "Click to Collapse");
        }

        $(this).tooltip('fixTitle');

    });

    // add hotkey to close all expanded parameter rows
    document.onkeyup = function (e) {
        if (e.key === '-') {
            $('.expanded').removeClass('expanded');
            let inRows = $('.in');
            inRows.addClass('out');
            inRows.removeClass('in');
        }
    };

    // process links to scroll inside the page instead of jump
    var $root = $('html, body');
    $('a').click(function () {
        var href = $.attr(this, 'href');

        clearTimeout(window.scrollTimeout);

        if (!href) { // flash & skip on toc links
            var reference = $(this).parent().attr("data-unique");

            if(!reference) {
                return true;
            }

            var heading = $("[name='" + reference + "']").next();

            heading.addClass("flash");
            setTimeout(function () {
                heading.removeClass("flash");
            }, 700);

            // workaround: scroll again after collapse/expand happened
            window.scrollTimeout = setTimeout(function () {
                $root.animate({
                    scrollTop: ($(heading).offset().top)
                }, 200);
            }, 400);

            $('.collapseToggle').not('.collapsed').each(function (i) {
                if ($(this).children('div[name="' + reference + '"]').length == 0) {
                    $(this).click();
                }
            });
            heading.parent(".collapseToggle.collapsed").click();

            return true;
        }

        if (href[0] !== '#') { // skip on and links to other pages
            return true;
        }

        var newLocation = $.attr(this, 'location');
        $root.animate({
            scrollTop: ($(href).offset().top)
        }, 300, function () {
            window.location.hash = newLocation;
        });

        return false;
    });

    //make TOC scroll with the page if needed
    $(window).scroll(function (e) {
        var scrollAmount = $(window).scrollTop() / ($(document).height() - $(window).height());
        var modifier = $('#toc').height() - Math.max(0.5, scrollAmount * scrollAmount) * $('#toc').height();

        $('#toc').scrollTop(scrollAmount * ($('#toc')[0].scrollHeight - $('#toc').height()) - modifier);
    });

    window.referencedParamTimeout = [];

    // highlight parameter references on mouseover
    $('.parameterHighlight').mouseover(function () {
        var reference = $(this).attr("name");
        $('[name="' + reference + '"]').addClass("highlight");

        clearTimeout(window.referencedParamTimeout['123412341234']);

        var referenceRow = $('tr[name="' + reference + '"]');

        if (referenceRow.parents('table.parameters').length) {
            if (window.referencedParamVisible) {
                showReferencedParam(referenceRow);
            } else {

                // delay stuff so the page doesn't jump when just moving the mouse over the page
                window.referencedParamTimeout[reference] = setTimeout(function () {
                    window.referencedParamVisible = true;
                    showReferencedParam(referenceRow);
                }, 300);
            }
        }
    });
    $('.parameterHighlight').mouseout(function () {
        var reference = $(this).attr("name");
        $('[name="' + reference + '"]').removeClass("highlight");

        clearTimeout(window.referencedParamTimeout[reference]);

        // delay this to show other params without delay when changed quickly
        window.referencedParamTimeout['123412341234'] = setTimeout(function () {
            window.referencedParamVisible = false;
        }, 300);

        var referenceRow = $('tr[name="' + reference + '"]');
        $(referenceRow).parents('tbody').find('tr').removeClass("inOverride");
        $('.fixedParameterRow').remove();

    });

    // read style from cookie
    var cookie = readCookie("neberus-style");
    var title = cookie
        ? cookie
        : getPreferredStyleSheet();
    setActiveStyleSheet(title);

    initFilterBox();

    // open usecase if page is loaded with anchor
    if (window.location.hash) {
        $('[name="' + window.location.hash.replace("#", "") + '"]').parent(".collapseToggle").click();
    }
});

$(window).load(function () {
    $("body").addClass("transition");
});

function setActiveStyleSheet(title) {
    var i,
        a,
        main;
    for (i = 0; (a = document.getElementsByTagName("link")[i]); i++) {
        if (a.getAttribute("rel").indexOf("style") != -1 && a.getAttribute("title")) {
            a.disabled = true;
            if (a.getAttribute("title") == title)
                a.disabled = false;
        }
    }
    createCookie("neberus-style", title, 365);
}

function getPreferredStyleSheet() {
    var i,
        a;
    for (i = 0; (a = document.getElementsByTagName("link")[i]); i++) {
        if (a.getAttribute("rel").indexOf("style") != -1 && a.getAttribute("rel").indexOf("alt") == -1 && a.getAttribute("title"))
            return a.getAttribute("title");
    }
    return null;
}

function createCookie(name, value, days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        var expires = "; expires=" + date.toGMTString();
    } else
        expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
}

function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document
        .cookie
        .split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ')
            c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0)
            return c.substring(nameEQ.length, c.length);
    }
    return null;
}

function initFilterBox() {
    var $filter = $('#filter');
    var $filterReset = $('#filterReset');

    var filter = function () {
        $filter.removeClass('error');

        var filter = $filter.val();

        filter = filter.replace(/\W/g, '\\$&');

        try {
            var regex = new RegExp(filter, 'i');
        } catch (e) {
            $filter.addClass('error');
            return;
        }

        $('.tocify-subheader > .tocify-item')
            .each(function (index, item) {
                var value = $(item).text();

                $(item).toggleClass('hidden', !regex.test(value));
            });
    };

    $filter.on('keyup', filter);

    $filterReset.click(function () {
        $filter.val("");
        $filter.keyup();
    });

    $filter.focus();
}

function isElementInViewport(el) {

    if (typeof jQuery === "function" && el instanceof jQuery) {
        el = el[0];
    }

    var rect = el.getBoundingClientRect();

    return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */
        rect.right <= (window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */
    );
}

function computedStyleToInlineStyle(element) {
    if (!element) {
        throw new Error("No element specified.");
    }

    if (typeof jQuery === "function" && element instanceof jQuery) {
        element = element[0];
    }

    $.each($(element).children(), function (index, child) {
        computedStyleToInlineStyle(child);
    });

    var computedStyle = getComputedStyle(element);

    $.each(computedStyle, function (index, property) {
        element.style[property] = computedStyle.getPropertyValue(property);
    });
}

function showReferencedParam(referenceRow) {
    if ($(referenceRow).hasClass('out')) {
        $(referenceRow).addClass("inOverride");

        var dataParent = $(referenceRow).data("toggle-parent");

        while (dataParent) {
            var toggleParent = $('[data-toggle-id="' + dataParent + '"');
            toggleParent.addClass("inOverride");
            dataParent = toggleParent.data("toggle-parent");
        }
    }

    if (!isElementInViewport(referenceRow)) {
        var cloned = referenceRow.clone().appendTo(referenceRow.closest('tbody')).removeClass("out").addClass("in");
        computedStyleToInlineStyle(cloned);
        cloned.addClass("fixedParameterRow");
    }
}
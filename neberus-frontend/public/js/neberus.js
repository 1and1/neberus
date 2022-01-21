
function findSchema(openApi, ref) {
    // if (ref in cachedRefs) {
    //     console.log("cached ref", ref, cachedRefs[ref]);
    //     return cachedRefs[ref];
    // }

    let parts = ref.replaceAll("#/", "").split("/");

    let current = openApi;

    parts.forEach(path => {
        // console.log("current: ", current);
        // console.log("path: ", path);
        current = current[path];
    })

    // cachedRefs[ref] = current;

    return current;
}


let resolveCache = {};

function resolveRefs(openApi, current, onlyProperties) {

    let result = {}
    // console.log("resolve: ", current);

    if (!current) {
        return result;
    }

    if (current.$ref) {
        if (current.$ref in resolveCache) {
            // console.log("cached resolve", current.$ref, resolveCache[current.$ref]);
            return resolveCache[current.$ref];
        }
        // reference
        let resolved = findSchema(openApi, current.$ref);
        result = resolveRefs(openApi, resolved.properties, true);
    } else if (current.properties) {
        // normal object
        result = resolveRefs(openApi, current.properties, true);
    } else if (current.enum) {
        // enum
        result = '{String}'
    } else if (current.type === 'array') {
        // array
        if (current.items.type === 'byte') {
            result = '{String}'
        } else {
            let item = resolveRefs(openApi, current.items, false);
            result = [item];
        }
    } else if (current.type === 'object' && current.additionalProperties) {
        // map
        let map = {};
        map['{String}'] = resolveRefs(openApi, current.additionalProperties, false);
        result = map;
    } else if (current.type === 'String' || current.type === 'int' || current.type === 'boolean') {
        result = '{' + current.type + '}';
    } else if (onlyProperties) {
        // only properties
        Object.keys(current).forEach(key => {

            if (key === 'exampleSetFlag' || key === 'extensions') {
                return;
            }

            let value = current[key];
            // console.log("process: ", key, value);
            // console.log(typeof value);


            // if (key === "$ref") {
            //     let resolved = findSchema(value);
            //     resolveRefs(resolved.properties, result);
            // } else
            if (typeof value === 'string') {
                value.__type = 'String';
                result[key] = value;
            } else {
                result[key] = resolveRefs(openApi, value, false);
            }

        });
    } else {
        // unresolved type
        result = current.type;
    }

    result.__type = current.type;

    resolveCache[current] = result;

    return result;
}

function getSchemaJson(openApi, schema) {
    let fullSchema = resolveRefs(openApi, schema);

    return JSON.stringify(fullSchema, undefined, 2)
        .replaceAll('"{int}"', '{int}')
        .replaceAll('"{boolean}"', '{boolean}');
}


function scrollToHeading(selected) {
    let root = jQuery('html, body');
    let heading = jQuery("#" + selected);

    // console.log(heading);

    if (jQuery(heading).offset()) {
        window.scrollTimeout = setTimeout(function () {
            root.animate({
                scrollTop: (jQuery(heading).offset().top - 30)
            }, 200);
        }, 0);
    }
}

function selectOperationAndScrollTo(selected) {
    const url = new URL(window.location);

    if (selected !== '') {
        url.searchParams.set('operation', selected);
        scrollToHeading(selected);
    } else {
        url.searchParams.delete('operation');
        scrollToTop();
    }

    window.history.pushState({}, '', url);
}

function selectUsecaseAndScrollTo(selected) {
    const url = new URL(window.location);

    if (selected !== '') {
        url.searchParams.set('usecases', selected);
        scrollToHeading('usecase-' + selected);
    } else {
        url.searchParams.delete('usecases');
        scrollToHeading('usecases-container');
    }

    window.history.pushState({}, '', url);
}

function scrollToTop() {
    window.scrollTimeout = setTimeout(function () {
        jQuery('html, body').animate({
            scrollTop: 0
        }, 200);
    }, 0);
}

window.referencedParamTimeout = [];

function highlightParameter(elem, event) {
    // console.log(event);
    event.stopPropagation();
    let reference = jQuery(elem).data("parameter-highlight-name");
    jQuery('[data-parameter-highlight-name="' + reference + '"]').addClass("highlight");

    clearTimeout(window.referencedParamTimeout['123412341234']);

    let referenceRow = jQuery('table.parameters tr[data-parameter-highlight-name="' + reference + '"]');

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
}

function deHighlightParameter(elem, event) {
    event.stopPropagation();
    let reference = jQuery(elem).data("parameter-highlight-name");
    jQuery('[data-parameter-highlight-name="' + reference + '"]').removeClass("highlight");

    clearTimeout(window.referencedParamTimeout[reference]);

    // delay this to show other params without delay when changed quickly
    window.referencedParamTimeout['123412341234'] = setTimeout(function () {
        window.referencedParamVisible = false;
    }, 300);

    let referenceRow = jQuery('table.parameters tr[data-parameter-highlight-name="' + reference + '"]');
    jQuery(referenceRow).parents('tbody').find('tr').removeClass("show-override");
    jQuery('.fixedParameterRow').remove();

}

function showReferencedParam(referenceRow) {
    if (!$(referenceRow).hasClass('show')) {
        $(referenceRow).addClass("show-override");

        let dataParent = $(referenceRow).data("toggle-parent");

        while (dataParent) {
            let toggleParent = $(dataParent);
            toggleParent.addClass("show-override");
            dataParent = toggleParent.data("toggle-parent");
        }
    }

    if (!isElementInViewport(referenceRow)) {
        var cloned = referenceRow.clone().appendTo(referenceRow.closest('tbody')).removeClass("out").addClass("in");
        computedStyleToInlineStyle(cloned);
        cloned.addClass("fixedParameterRow");
    }
}

function toJsonPretty(fullSchema) {
    return JSON.stringify(fullSchema,
        (k, v) => {
            if (k.startsWith('__')) {
                return undefined;
            } else {
                return v;
            }
        }, 2)
        .replaceAll('"{int}"', '{int}')
        .replaceAll('"{boolean}"', '{boolean}');
}

function toFullXmlPretty(o, root) {
    root = root[0] + root.substring(1);

    let xmlSchema = '&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;\n';
    xmlSchema += '&lt;' + root + '&gt;\n';
    xmlSchema += toXmlPretty(o, 1);
    xmlSchema += '&lt;/' + root + '&gt;';

    return xmlSchema;
}

function toXmlPretty(o, level) {
    let indent = '  '.repeat(level);
    let xmlSchema = '';

    if (!o) {
        return xmlSchema;
    }

    if (o.__type === 'array') {
        let child = toXmlPretty(o[0], level + 1);

        if (child.trim().split('\n').length > 1 || child.split('&lt;').length > 1) {
            xmlSchema += indent + '&lt;element&gt;\n';
            xmlSchema += child;
            xmlSchema += indent + '&lt;/element&gt;\n';
        } else {
            xmlSchema += indent + '&lt;element&gt;';
            xmlSchema += child.trim();
            xmlSchema += '&lt;/element&gt;\n';
        }

    } else if (o.__type === 'object') {
        let child = toXmlPretty(o['{String}'], level + 1);

        if (child.trim().split('\n').length > 1 || child.split('&lt;').length > 1) {
            xmlSchema += indent + '&lt;{String}&gt;\n';
            xmlSchema += child;
            xmlSchema += indent + '&lt;/{String}&gt;\n';
        } else {
            xmlSchema += indent + '&lt;{String}&gt;';
            xmlSchema += child.trim();
            xmlSchema += '&lt;/{String}&gt;\n';
        }

    } else if (o.__type === 'String' || o.__type === 'int' || o.__type === 'boolean') {
        xmlSchema += '{' + o.__type + '}' + '\n';
    } else if (typeof o === 'string') {
        xmlSchema += o + '\n';
    } else {
        Object.keys(o).filter(key => !key.startsWith('__')).forEach(key => {

            let child = toXmlPretty(o[key], level + 1);

            if (child.trim().split('\n').length > 1 || child.split('&lt;').length > 1) {
                xmlSchema += indent + '&lt;' + key + '&gt;\n';
                xmlSchema += child;
                xmlSchema += indent + '&lt;/' + key + '&gt;\n';
            } else {
                xmlSchema += indent + '&lt;' + key + '&gt;';
                xmlSchema += child.trim();
                xmlSchema += '&lt;/' + key + '&gt;\n';
            }

        });
    }

    return xmlSchema;
}

function toFormUrlencodedPretty(o, parent) {
    let formSchema = '';

    if (!o) {
        return formSchema;
    }

    if (o.__type === 'array') {
        formSchema += toFormUrlencodedPretty(o[0], parent + '[0]');
    } else if (o.__type === 'object') {
        formSchema += toFormUrlencodedPretty(o['{String}'], parent + '[{String}]');
    } else if (o.__type === 'String' || o.__type === 'int' || o.__type === 'boolean') {
        formSchema += parent + '={' + o.__type + '}';
    } else if (typeof o === 'string') {
        formSchema += parent + '=' + o;
    } else {
        Object.keys(o).filter(key => !key.startsWith('__')).forEach(key => {

            // formSchema += key + '={' + o[key].__type + '}';
            let newParent = parent === '' ? key : (parent + '[' + key + ']');
            let child = toFormUrlencodedPretty(o[key], newParent);

            if (child !== '') {
                formSchema += child;
                // formSchema += '<span class="word-wrap"></span>&';
                formSchema += '&';
            }
        });
        formSchema = formSchema.substr(0, formSchema.length - 1);
    }

    return formSchema;
}

function initPopoverButton(el) {
    return new bootstrap.Popover(el);
}

function initTooltipBox(el) {
    return new bootstrap.Tooltip(el);
}

function initCollapseToggle(el) {
    if (jQuery(el).attr("data-bs-toggle") !== "collapse") {
        return;
    }

    jQuery(el.getAttribute("data-bs-target"))[0].addEventListener('hide.bs.collapse', function () {
        jQuery(el.getAttribute("data-bs-target") + '-control[aria-expanded="true"]').click();
    })
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

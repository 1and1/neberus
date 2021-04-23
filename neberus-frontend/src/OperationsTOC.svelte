<script>

    import {afterUpdate} from 'svelte';

    export let paths;
    export let resource;
    export let openApi;

    const selectOperation = (event) => {
        let selected = event.target.dataset.operation;

        selectOperationAndScrollTo(selected);
    }

    function initFilterBox() {
        let jQueryfilter = jQuery('#filter');
        let jQueryfilterReset = jQuery('#filterReset');

        let filter = function () {
            jQueryfilter.removeClass('error');

            let filter = jQueryfilter.val();

            filter = filter.replace(/\W/g, '\\$&');

            try {
                let regex = new RegExp(filter, 'i');

                jQuery('.list-group-item')
                    .each(function (index, item) {
                        let value = jQuery(item).text();

                        jQuery(item).toggleClass('hidden', !regex.test(value));
                    });
            } catch (e) {
                jQueryfilter.addClass('error');
            }
        };

        jQueryfilter.on('keyup', filter);

        jQueryfilterReset.click(function () {
            jQueryfilter.val("");
            jQueryfilter.keyup();
        });

        jQueryfilter.focus();
    }

    afterUpdate(async () => {
        if (paths) {
            initFilterBox();
        }
    });

</script>


{#if resource && resource !== '' && paths}

    <form class="form-inline w-100" autocomplete="off">
        <div class="form-group filterBox w-100">
            <div class="input-group filterBox w-100">
                <input class="filterBox w-100" id="filter" placeholder="Search" type="text">
                <i id="filterReset" class="fas fa-times form-control-feedback"></i>
            </div>
        </div>
    </form>
    <div id="nav-operations" class="list-group">
        <span class="list-group-item list-group-item-action}"
           data-operation=""
           on:click={selectOperation}>
            {#if openApi.extensions && openApi.extensions['x-resources-metadata'][resource].label}
                {openApi.extensions['x-resources-metadata'][resource].label}
            {:else}
                {resource}
            {/if}
        </span>
        {#each Object.keys(paths) as path}
            {#each Object.keys(paths[path]) as method}
                <span class="list-group-item list-group-item-action list-group-item-indent {paths[path][method].deprecated?'operation-toc-deprecated':''}"
                   data-operation="{method.toUpperCase()}-{paths[path][method].summary.replaceAll(/[^A-Za-z0-9]/g, '_')}"
                   on:click={selectOperation}>
                    {method.toUpperCase()} - {paths[path][method].summary}
                </span>
            {/each}
        {/each}

    </div>

{/if}


<style>

    .operation-toc-deprecated {
        text-decoration: line-through;
    }

    .filterBox {
        padding-bottom: 5px;
    }

    .list-group-item {
        font-size: 0.9em;
        padding: 0.5rem 0.75rem;
    }

    .list-group-item-indent {
        padding-left: 25px;
    }

</style>
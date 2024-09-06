<script>

    export let paths;
    export let resource;
    export let openApi;

    const selectOperation = (event) => {
        let selected = event.target.dataset.operation;

        selectOperationAndScrollTo(selected);
    }

    let filter = function (filter) {
        filter = filter.replace(/\W/g, '\\$&');

        try {
            let regex = new RegExp(filter, 'i');

            jQuery('.list-group-item')
                .each(function (index, item) {
                    let value = jQuery(item).text();

                    jQuery(item).toggleClass('hidden', !regex.test(value));
                });
        } catch (e) {
            console.log(e);
        }
    };

    export let handleSubmit = function (event) {
        const {value} = this;
        filter(value);
    };

    export let handleReset = function (event) {
        jQuery('#filter').val('');
        filter('');
    };

</script>


{#if resource && resource !== '' && paths}

    <form class="form-inline w-100" autocomplete="off">
        <div class="form-group filterBox w-100">
            <div class="input-group filterBox w-100">
                <input class="filterBox w-100" id="filter" placeholder="Search..." on:keyup|preventDefault={handleSubmit} type="text">
                <span on:click|preventDefault={handleReset}><i id="filterReset" class="fas fa-times form-control-feedback"></i></span>
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
                   data-operation="{paths[path][method].operationId}"
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
        border: 1px solid rgba(0,0,0,0.25);
    }

    .list-group-item-indent {
        padding-left: 25px;
    }

    #nav-operations {
        max-height: 90%;
        overflow: auto;
    }

</style>

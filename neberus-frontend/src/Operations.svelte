<script>
    import PathOverview from "./PathOverview.svelte";
    import Operation from "./Operation.svelte";
    import queryString from "query-string";
    import {afterUpdate} from "svelte";

    export let paths;
    export let openApi;

    afterUpdate(async () => {
        if (window) {
            let queryParams = queryString.parse(location.search);
            if (queryParams.operation) {
                selectOperationAndScrollTo(queryParams.operation);
            } else {
                scrollToTop();
            }
        }
    })

</script>

<PathOverview paths={paths}/>

<div data-spy="scroll" data-bs-target="#nav-operations" data-offset="0" class="scrollspy-example">
    {#if paths}
        {#each Object.keys(paths) as path}
            {#each Object.keys(paths[path]) as method}
                <Operation path={path} method={method} operation={paths[path][method]} openApi={openApi}/>
            {/each}
        {/each}
    {/if}
</div>

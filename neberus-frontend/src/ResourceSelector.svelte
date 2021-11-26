<script>
    import {createEventDispatcher} from 'svelte';
    import queryString from "query-string";

    export let resources;
    export let openApi;

    let selected;

    let queryParams = queryString.parse(location.search);

    const dispatch = createEventDispatcher();
    const selectResource = () => {
        const url = new URL(window.location);

        if (selected === "") {
            url.searchParams.delete('resource');
        } else {
            url.searchParams.set('resource', selected);
        }

        url.searchParams.delete('operation');

        window.history.pushState({}, '', url);
        // window.location = url;

        dispatch('click', selected);
    }

    if (queryParams.resource) {
        selected = queryParams.resource;
    }

</script>

{#if resources}
    <select bind:value={selected} on:change={selectResource} class="form-select custom-select mb-3">
        <option value="" selected>Overview</option>
        {#each resources as resource}
            <option value="{resource}">Resource -
                {openApi.extensions && openApi.extensions['x-resources-metadata'][resource].label
                    ? openApi.extensions['x-resources-metadata'][resource].label
                    : resource}
            </option>
        {/each}
    </select>
    <i class="fa fa-caret-down select-caret" aria-hidden="true"></i>
{/if}


<style>
    .custom-select {
        padding-right: 0.5rem;
        padding-top: 10px;
        padding-bottom: 10px;
        background-color: #1c1e22;
    }

    .select-caret {
        float: right;
        margin-top: -45px;
        margin-right: 10px;
        position: relative;
    }

</style>

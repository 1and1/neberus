<script>
    import {onMount} from "svelte";
    import ResourceInfo from "./ResourceInfo.svelte";
    import ResourceSelector from "./ResourceSelector.svelte";
    import Operations from "./Operations.svelte";
    import ServiceInfo from "./ServiceInfo.svelte";
    import OperationsTOC from "./OperationsTOC.svelte";
    import queryString from "query-string";
    import Usecases from "./Usecases.svelte";
    import UsecaseTOC from "./UsecaseTOC.svelte";
    import ServiceInfoTOC from "./ServiceInfoTOC.svelte";

    $: queryParams = queryString.parse(location.search);

    $: openApi = [];
    $: resources = [];
    $: activeResource = "";
    $: activePaths = [];

    onMount(loadOpenApiJson);

    async function loadOpenApiJson() {
        const json = JSON.parse(openApiJsonString); // openApiJsonString from generated openApi.js

        let paths = Object.keys(json.paths);

        paths.forEach(path => {
            let operations = Object.keys(json.paths[path]);

            operations.forEach(operation => {
                let tags = json.paths[path][operation].tags.filter(t => t.startsWith("resource:"));
                let resource = tags[0].replace('resource:', '');

                if (!resources.includes(resource)) {
                    resources.push(resource);
                }
            });
        });
        resources = resources; // update field for svelte

        openApi = json;

        document.title = openApi.info.title;

        if (queryParams.resource) {
            updateActiveResource(queryParams.resource);
        }
    }

    $: paths = openApi.paths;

    function updateActiveResource(resource) {
        activeResource = resource

        let paths = Object.keys(openApi.paths);
        let newActivePaths = [];

        paths.forEach(path => {
            let methods = Object.keys(openApi.paths[path]);

            methods.filter(method => openApi.paths[path][method].tags.includes("resource:" + activeResource))
                .forEach(method => {
                    if (!newActivePaths.includes(path)) {
                        newActivePaths[path] = {};
                    }

                    newActivePaths[path][method] = openApi.paths[path][method];
                });
        });

        activePaths = Object.keys(newActivePaths).length === 0 ? undefined : newActivePaths;

        document.title = openApi.info.title;

        if (activeResource && activeResource !== ''
            && openApi.extensions && openApi.extensions['x-resources-metadata'] && openApi.extensions['x-resources-metadata'][activeResource]) {
            let resourceLabel = openApi.extensions['x-resources-metadata'][activeResource].label;
            document.title += ' - ' + resourceLabel;
        }

    }

    export let updateResource = function (event) {
        updateActiveResource(event.detail);
    }

</script>

<div class="">
    <div class="navigationContainer">
        <ResourceSelector resources={resources} openApi={openApi} on:click={updateResource}/>
        <OperationsTOC paths={activePaths} resource={activeResource} openApi={openApi}/>
        {#if !activeResource}
            <ServiceInfoTOC openApi={openApi}/>
            <UsecaseTOC openApi={openApi}/>
        {/if}
    </div>
    <div class="serviceContainer">
        {#if activeResource}
            <ResourceInfo resource={activeResource} openApi={openApi}/>
            <Operations paths={activePaths} openApi={openApi}/>
        {:else}
            <ServiceInfo openApi={openApi}/>
            <Usecases openApi={openApi}/>
        {/if}
    </div>
</div>

<style>

</style>
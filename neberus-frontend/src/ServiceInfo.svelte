<script>
    export let openApi;
</script>

{#if openApi && openApi.info}
    <div class="">
        <h1>{openApi.info.title} - {openApi.info.version}</h1>

        {#if openApi.extensions && openApi.extensions['x-resources-metadata']}
            <div class="card">
                <div class="card-body">
                    <h2>Resources</h2>
                    <ul class="resources-list">
                        {#each Object.keys(openApi.extensions['x-resources-metadata']) as resource}
                            <li>
                                <a href="?resource={resource}">{openApi.extensions['x-resources-metadata'][resource].label}</a>
                                {#if openApi.extensions['x-resources-metadata'][resource].shortDescription}
                                    <br><div class="resource-short-description">{@html openApi.extensions['x-resources-metadata'][resource].shortDescription}</div>
                                {/if}
                            </li>
                        {/each}
                    </ul>
                </div>
            </div>
        {/if}

        {#if openApi.info.description}
            <div class="card">
                <div class="card-body">
                    {@html openApi.info.description}
                </div>
            </div>
        {/if}
    </div>
{/if}


<style>

    ul.resources-list {
        line-height: 2;
        margin-bottom: 0px;
    }

    .resource-short-description {
        padding-left: 50px;
        line-height: 1.3;
    }

</style>
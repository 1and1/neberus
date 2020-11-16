<script>
    export let openApi;
    export let resource;
</script>

{#if resource}
    {#if openApi.extensions['x-resources-metadata'][resource].label}
        <h1>{openApi.extensions['x-resources-metadata'][resource].label}</h1>
    {:else}
        <h1>{resource}</h1>
    {/if}

    {#if openApi.extensions['x-resources-metadata'][resource].description}
        <div class="card card-primary">
            <div class="card-body">{@html openApi.extensions['x-resources-metadata'][resource].description}</div>
        </div>
    {/if}
{/if}

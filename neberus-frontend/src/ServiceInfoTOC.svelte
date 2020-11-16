<script>
    export let openApi;
</script>

{#if openApi && openApi.info}
    <div id="nav-service-info" class="list-group">
        <span class="list-group-item list-group-item-action}" on:click={scrollToTop}>
            {openApi.info.title} - {openApi.info.version}
        </span>
    </div>
{/if}

<style>

    .list-group-item {
        font-size: 0.9em;
        padding: 0.5rem 0.75rem;
    }

    #nav-service-info {
        margin-bottom: 15px;
    }

</style>
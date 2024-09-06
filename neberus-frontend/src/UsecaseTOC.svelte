<script>

    export let openApi;

    const selectUsecase = (event) => {
        let selected = event.target.dataset.usecase;

        selectUsecaseAndScrollTo(selected);

        jQuery('.usecase > .highlight').removeClass('highlight');
        jQuery('#usecase-' + selected + ' > .card-header').addClass('highlight');
    }

</script>


{#if openApi.extensions && openApi.extensions['x-usecases']}

    <div id="nav-usecases" class="list-group">
        <span class="list-group-item list-group-item-action}"
           data-usecase=""
           on:click={selectUsecase}>
            Usecases
        </span>
        {#each Object.keys(openApi.extensions['x-usecases'].usecases) as usecaseId}
            <span class="list-group-item list-group-item-action list-group-item-indent"
                  data-usecase={usecaseId} on:click={selectUsecase}>
                {openApi.extensions['x-usecases'].usecases[usecaseId].name}
            </span>
        {/each}

    </div>
{/if}


<style>

    .list-group-item {
        font-size: 0.9em;
        padding: 0.5rem 0.75rem;
        border: 1px solid rgba(0,0,0,0.25);
    }

    .list-group-item-indent {
        padding-left: 25px;
    }

</style>

<script>
    import UsecaseMethod from "./UsecaseMethod.svelte";
    import queryString from "query-string";
    import {afterUpdate} from "svelte";

    export let openApi;

    function shouldHighlightUsecase(usecaseId) {
        let queryParams = queryString.parse(location.search);
        let usecaseList = queryParams['usecases'];

        if (!usecaseList || usecaseList.length === 0) {
            return false;
        }

        return usecaseList.split(',').includes(usecaseId);
    }

    const initCollapse = el => {
        initCollapseToggle(el);
    }

    afterUpdate(async() => {
        if (window) {
            let queryParams = queryString.parse(location.search);
            if (queryParams.usecases) {
                scrollToHeading('usecases-container');
            } else {
                scrollToTop();
            }
        }
    })

</script>

{#if openApi.extensions && openApi.extensions['x-usecases']}
    <div class="card card-primary usecases-container">
        <h3>Usecases</h3>
        <div>{@html openApi.extensions['x-usecases'].description}</div>

        <div>
            {#each Object.keys(openApi.extensions['x-usecases'].usecases) as usecaseId}
                <div class="card card-primary usecase" id="usecase-{usecaseId}">
                    <div class="card-header bg-secondary collapsed {shouldHighlightUsecase(usecaseId) ? 'highlight' : ''}"
                         data-toggle="collapse" data-target=".usecase-body-{usecaseId}" aria-expanded="false">
                        <span>{openApi.extensions['x-usecases'].usecases[usecaseId].name}</span>
                        <span>
                            <i class="icon-toggle fas fa-angle-right"></i>
                        </span>
                    </div>

                    <div class="card-body usecase-body-{usecaseId} collapse">
                        <div class="card-text">
                            <div class="usecase-description">{@html openApi.extensions['x-usecases'].usecases[usecaseId].description}</div>

                            {#each openApi.extensions['x-usecases'].usecases[usecaseId].methods as method}
                                <UsecaseMethod openApi={openApi} method={method}/>
                            {/each}

                        </div>
                    </div>
                </div>
            {/each}
        </div>
    </div>
{/if}

<style>

    .usecase.card {
        border: 0px;
        box-shadow: 4px 4px 10px 0px #0000007a;
    }

    .usecase-description {
        padding: 10px;
    }

    .usecases-container {
        min-height: 20px;
        padding: 19px;
        margin-bottom: 20px;
    }

</style>

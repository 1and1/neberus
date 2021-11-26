<script>
    import OpenApi from "./OpenApi.svelte";
    import {afterUpdate} from "svelte";

    afterUpdate(async () => {

        jQuery(function() {

            // close popovers when clicking outside
            jQuery(document).on('click', function (e) {
                jQuery('[data-bs-toggle="popover"]')
                    .each(function () {
                        // the 'is' for buttons that trigger popups the 'has' for icons within a button that triggers a popup
                        if (!jQuery(this).is(e.target) && jQuery(this).has(e.target).length === 0 && jQuery('.popover').has(e.target).length === 0) {
                            bootstrap.Popover.getInstance(this).hide();
                        }
                    });
            });

        });

    });

</script>

<main>
    <OpenApi/>
    <div class="credits">Proudly presented by <a href="https://github.com/1and1/neberus">Neberus</a></div>
</main>

<style>
    .credits {
        float: right;
        width: 100vw;
        text-align: right;
        padding-bottom: 10px;
        padding-right: 30px;
        margin-top: -10px;
    }
</style>
